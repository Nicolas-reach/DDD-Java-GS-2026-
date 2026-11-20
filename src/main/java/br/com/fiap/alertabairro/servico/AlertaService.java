package br.com.fiap.alertabairro.servico;

import br.com.fiap.alertabairro.dominio.NivelRisco;
import br.com.fiap.alertabairro.dominio.monitoramento.Alerta;
import br.com.fiap.alertabairro.dominio.monitoramento.Medicao;
import br.com.fiap.alertabairro.dominio.monitoramento.OrigemAlerta;
import br.com.fiap.alertabairro.dominio.monitoramento.Sensor;
import br.com.fiap.alertabairro.dominio.ocorrencia.Ocorrencia;
import br.com.fiap.alertabairro.dominio.territorio.Bairro;
import br.com.fiap.alertabairro.dominio.usuario.Usuario;
import br.com.fiap.alertabairro.notificacao.CanalNotificacao;
import br.com.fiap.alertabairro.repositorio.Repositorio;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Emite, divulga e encerra alertas comunitários.
 * <p>
 * Um alerta nasce de duas formas:
 * <ol>
 *   <li>uma leitura de sensor em ATENÇÃO ou CRÍTICO ({@link #emitirPorMedicao});</li>
 *   <li>ao menos {@value #MINIMO_PARA_ATENCAO} ocorrências de risco no mesmo bairro em
 *       {@value #JANELA_HORAS} horas ({@link #avaliarConcentracao}).</li>
 * </ol>
 * Para não repetir o mesmo aviso, um alerta só é emitido se não houver outro ativo,
 * da mesma origem, com nível igual ou maior.
 */
public class AlertaService {

    /** Tamanho da janela usada para medir a concentração de ocorrências, em horas. */
    public static final int JANELA_HORAS = 2;
    /** Ocorrências de risco na janela que geram alerta de atenção. */
    public static final int MINIMO_PARA_ATENCAO = 3;
    /** Ocorrências de risco na janela que geram alerta crítico. */
    public static final int MINIMO_PARA_CRITICO = 5;

    private final Repositorio<Alerta> alertas;
    private final Repositorio<Ocorrencia> ocorrencias;
    private final Repositorio<Usuario> usuarios;
    private final List<CanalNotificacao> canais;
    private final Clock relogio;

    /**
     * Cria o serviço.
     *
     * @param alertas     repositório de alertas
     * @param ocorrencias repositório de ocorrências (para medir concentração)
     * @param usuarios    repositório de usuários (para achar os moradores do bairro)
     * @param canais      canais por onde os alertas são divulgados
     * @param relogio     relógio do sistema (permite simular horários)
     */
    public AlertaService(Repositorio<Alerta> alertas, Repositorio<Ocorrencia> ocorrencias,
                         Repositorio<Usuario> usuarios, List<CanalNotificacao> canais, Clock relogio) {
        this.alertas = alertas;
        this.ocorrencias = ocorrencias;
        this.usuarios = usuarios;
        this.canais = List.copyOf(canais);
        this.relogio = relogio;
    }

    /**
     * Emite um alerta para uma leitura de sensor, se ela pedir e se ainda não houver
     * alerta ativo do mesmo sensor com nível igual ou maior.
     *
     * @param medicao leitura recém-registrada
     * @return o alerta emitido, ou vazio
     */
    public Optional<Alerta> emitirPorMedicao(Medicao medicao) {
        if (!medicao.getNivelRisco().exigeAlerta()) {
            return Optional.empty();
        }
        Sensor sensor = medicao.getSensor();
        boolean jaCoberto = !alertas.filtrar(a -> a.isAtivo()
                && a.getSensorOrigem().map(sensor::equals).orElse(false)
                && a.getNivel().isPeloMenos(medicao.getNivelRisco())).isEmpty();
        if (jaCoberto) {
            return Optional.empty();
        }
        String texto = medicao.descrever() + " em " + sensor.getLocalizacao().getDescricaoCurta() + "."
                + (medicao.getNivelRisco() == NivelRisco.CRITICO ? " Evite o local." : "");
        return Optional.of(emitir(Alerta.porMedicao(medicao, texto)));
    }

    /**
     * Verifica se há concentração de ocorrências de risco no bairro e, se houver,
     * emite um alerta.
     *
     * @param bairro bairro a avaliar
     * @return o alerta emitido, ou vazio
     */
    public Optional<Alerta> avaliarConcentracao(Bairro bairro) {
        LocalDateTime agora = LocalDateTime.now(relogio);
        LocalDateTime inicio = agora.minus(Duration.ofHours(JANELA_HORAS));
        long quantidade = ocorrencias.filtrar(o -> o.getBairro().equals(bairro)
                && o.isAtiva()
                && o.registradaEntre(inicio, agora.plusSeconds(1))
                && o.classificarRisco().isPeloMenos(NivelRisco.ATENCAO)).size();

        NivelRisco nivel = quantidade >= MINIMO_PARA_CRITICO ? NivelRisco.CRITICO
                : quantidade >= MINIMO_PARA_ATENCAO ? NivelRisco.ATENCAO
                : NivelRisco.NORMAL;
        if (!nivel.exigeAlerta()) {
            return Optional.empty();
        }
        boolean jaCoberto = !alertas.filtrar(a -> a.isAtivo()
                && a.getOrigem() == OrigemAlerta.OCORRENCIAS
                && a.getBairro().equals(bairro)
                && a.getNivel().isPeloMenos(nivel)).isEmpty();
        if (jaCoberto) {
            return Optional.empty();
        }
        String texto = String.format("%d ocorrências de risco registradas nas últimas %d h no bairro %s.",
                quantidade, JANELA_HORAS, bairro.getNome());
        return Optional.of(emitir(Alerta.porConcentracao(bairro, nivel, texto, agora)));
    }

    private Alerta emitir(Alerta alerta) {
        alertas.salvar(alerta);
        List<Usuario> moradores = usuarios.filtrar(u -> u.isAtivo() && u.moraEm(alerta.getBairro()));
        for (CanalNotificacao canal : canais) {
            canal.enviar(alerta, moradores);
        }
        return alerta;
    }

    /**
     * Encerra um alerta.
     *
     * @param idAlerta      alerta a encerrar
     * @param idResponsavel quem encerra
     * @return o alerta encerrado
     */
    public Alerta encerrar(long idAlerta, long idResponsavel) {
        Alerta alerta = alertas.buscarPorIdOuFalhar(idAlerta);
        Usuario responsavel = usuarios.buscarPorIdOuFalhar(idResponsavel);
        alerta.encerrar(responsavel, LocalDateTime.now(relogio));
        return alerta;
    }

    /**
     * Encerra todos os alertas ativos de um bairro, quando a situação normaliza.
     *
     * @param bairro        bairro
     * @param idResponsavel quem encerra
     * @return quantos alertas foram encerrados
     */
    public int encerrarAtivosDoBairro(Bairro bairro, long idResponsavel) {
        Usuario responsavel = usuarios.buscarPorIdOuFalhar(idResponsavel);
        List<Alerta> ativos = alertas.filtrar(a -> a.isAtivo() && a.getBairro().equals(bairro));
        LocalDateTime agora = LocalDateTime.now(relogio);
        ativos.forEach(a -> a.encerrar(responsavel, agora));
        return ativos.size();
    }

    /**
     * Retorna alertas ativos, do mais grave para o menos grave.
     *
     * @return alertas ativos, do mais grave para o menos grave
     */
    public List<Alerta> listarAtivos() {
        return alertas.filtrar(Alerta::isAtivo).stream()
                .sorted(Comparator.comparing(Alerta::getNivel).reversed()
                        .thenComparing(Alerta::getDataEmissao))
                .toList();
    }

    /**
     * Retorna todos os alertas, na ordem de emissão.
     *
     * @return todos os alertas, na ordem de emissão
     */
    public List<Alerta> listarTodos() {
        return alertas.listarTodos();
    }
}

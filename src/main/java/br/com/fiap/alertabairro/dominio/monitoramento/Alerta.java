package br.com.fiap.alertabairro.dominio.monitoramento;

import br.com.fiap.alertabairro.dominio.EntidadeBase;
import br.com.fiap.alertabairro.dominio.NivelRisco;
import br.com.fiap.alertabairro.dominio.Validacao;
import br.com.fiap.alertabairro.dominio.excecao.AcessoNegadoException;
import br.com.fiap.alertabairro.dominio.excecao.RegraNegocioException;
import br.com.fiap.alertabairro.dominio.territorio.Bairro;
import br.com.fiap.alertabairro.dominio.usuario.Permissao;
import br.com.fiap.alertabairro.dominio.usuario.Usuario;
import br.com.fiap.alertabairro.util.Formato;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Alerta comunitário emitido para um bairro (tabela ALERTA).
 * <p>
 * <b>Importante:</b> é um indicador experimental da comunidade. Toda mensagem
 * termina com {@link #AVISO_NAO_OFICIAL}, para não ser confundida com um alerta
 * oficial da Defesa Civil.
 * <p>
 * Os alertas são criados por dois métodos de fábrica, um para cada origem.
 * O construtor é privado e garante a mesma regra do banco: alerta de SENSOR tem
 * medição; alerta de OCORRENCIAS não tem.
 */
public class Alerta extends EntidadeBase {

    /** Aviso anexado a toda mensagem de alerta. */
    public static final String AVISO_NAO_OFICIAL =
            "Indicador comunitário, não é alerta oficial. Em emergência ligue 199 (Defesa Civil).";

    private final NivelRisco nivel;
    private final OrigemAlerta origem;
    private final String mensagem;
    private final Bairro bairro;
    private final Medicao medicao;
    private final LocalDateTime dataEmissao;
    private LocalDateTime dataEncerramento;

    private Alerta(NivelRisco nivel, OrigemAlerta origem, String texto, Bairro bairro, Medicao medicao,
                   LocalDateTime dataEmissao) {
        this.nivel = Validacao.obrigatorio(nivel, "Nível do alerta");
        if (!nivel.exigeAlerta()) {
            throw new RegraNegocioException("Um alerta precisa ser de nível Atenção ou Crítico.");
        }
        this.origem = Validacao.obrigatorio(origem, "Origem do alerta");
        if ((origem == OrigemAlerta.SENSOR) != (medicao != null)) {
            throw new RegraNegocioException("Alerta de sensor precisa da medição; alerta de ocorrências não tem medição.");
        }
        this.mensagem = Validacao.texto(texto + " " + AVISO_NAO_OFICIAL, "Mensagem do alerta", 10, 300);
        this.bairro = Validacao.obrigatorio(bairro, "Bairro do alerta");
        this.medicao = medicao;
        this.dataEmissao = Validacao.obrigatorio(dataEmissao, "Data de emissão");
    }

    /**
     * Cria um alerta a partir de uma leitura de sensor acima do limite.
     *
     * @param medicao leitura que disparou o alerta
     * @param texto   texto principal da mensagem
     * @return novo alerta, com o nível da medição
     */
    public static Alerta porMedicao(Medicao medicao, String texto) {
        Validacao.obrigatorio(medicao, "Medição");
        return new Alerta(medicao.getNivelRisco(), OrigemAlerta.SENSOR, texto, medicao.getSensor().getBairro(),
                medicao, medicao.getDataMedicao());
    }

    /**
     * Cria um alerta pela concentração de ocorrências em um bairro.
     *
     * @param bairro bairro afetado
     * @param nivel  ATENCAO ou CRITICO
     * @param texto  texto principal da mensagem
     * @param quando data e hora da emissão
     * @return novo alerta
     */
    public static Alerta porConcentracao(Bairro bairro, NivelRisco nivel, String texto, LocalDateTime quando) {
        return new Alerta(nivel, OrigemAlerta.OCORRENCIAS, texto, bairro, null, quando);
    }

    /**
     * Encerra o alerta.
     *
     * @param responsavel quem encerra (precisa da permissão {@link Permissao#ENCERRAR_ALERTA})
     * @param quando      data e hora do encerramento
     * @throws AcessoNegadoException se o responsável não pode encerrar alertas
     * @throws RegraNegocioException se o alerta já estava encerrado
     */
    public void encerrar(Usuario responsavel, LocalDateTime quando) {
        Validacao.obrigatorio(responsavel, "Responsável");
        if (!responsavel.temPermissao(Permissao.ENCERRAR_ALERTA)) {
            throw new AcessoNegadoException(responsavel.getNome() + " (" + responsavel.getPerfil().getDescricao()
                    + ") não tem permissão para encerrar alertas.");
        }
        if (!isAtivo()) {
            throw new RegraNegocioException("O alerta #" + getId() + " já foi encerrado.");
        }
        if (quando.isBefore(dataEmissao)) {
            throw new RegraNegocioException("O encerramento não pode ser anterior à emissão.");
        }
        this.dataEncerramento = quando;
    }

    /**
     * Retorna {@code true} enquanto não foi encerrado.
     *
     * @return {@code true} enquanto não foi encerrado
     */
    public boolean isAtivo() {
        return dataEncerramento == null;
    }

    /**
     * Retorna sensor que originou o alerta, se a origem for SENSOR.
     *
     * @return sensor que originou o alerta, se a origem for SENSOR
     */
    public Optional<Sensor> getSensorOrigem() {
        return medicao == null ? Optional.empty() : Optional.of(medicao.getSensor());
    }

    /**
     * Retorna nível do alerta.
     *
     * @return nível do alerta
     */
    public NivelRisco getNivel() {
        return nivel;
    }

    /**
     * Retorna origem do alerta.
     *
     * @return origem do alerta
     */
    public OrigemAlerta getOrigem() {
        return origem;
    }

    /**
     * Retorna mensagem completa, já com o aviso de que não é oficial.
     *
     * @return mensagem completa, já com o aviso de que não é oficial
     */
    public String getMensagem() {
        return mensagem;
    }

    /**
     * Retorna bairro avisado.
     *
     * @return bairro avisado
     */
    public Bairro getBairro() {
        return bairro;
    }

    /**
     * Retorna medição de origem, se houver.
     *
     * @return medição de origem, se houver
     */
    public Optional<Medicao> getMedicao() {
        return Optional.ofNullable(medicao);
    }

    /**
     * Retorna data e hora de emissão.
     *
     * @return data e hora de emissão
     */
    public LocalDateTime getDataEmissao() {
        return dataEmissao;
    }

    /**
     * Retorna data e hora de encerramento, ou {@code null} se ativo.
     *
     * @return data e hora de encerramento, ou {@code null} se ativo
     */
    public LocalDateTime getDataEncerramento() {
        return dataEncerramento;
    }

    @Override
    public String resumo() {
        String situacao = isAtivo() ? "ativo" : "encerrado em " + Formato.diaHora(dataEncerramento);
        return String.format("Alerta #%d %s | %s | %s | emitido %s | %s",
                getId(), nivel.getDescricao().toUpperCase(), bairro.getNome(), origem.getDescricao(),
                Formato.diaHora(dataEmissao), situacao);
    }
}

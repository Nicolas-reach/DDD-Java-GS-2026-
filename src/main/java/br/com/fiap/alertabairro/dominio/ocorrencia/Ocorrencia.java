package br.com.fiap.alertabairro.dominio.ocorrencia;

import br.com.fiap.alertabairro.dominio.EntidadeBase;
import br.com.fiap.alertabairro.dominio.NivelRisco;
import br.com.fiap.alertabairro.dominio.Validacao;
import br.com.fiap.alertabairro.dominio.excecao.AcessoNegadoException;
import br.com.fiap.alertabairro.dominio.excecao.RegraNegocioException;
import br.com.fiap.alertabairro.dominio.territorio.Bairro;
import br.com.fiap.alertabairro.dominio.territorio.Localizacao;
import br.com.fiap.alertabairro.dominio.usuario.Permissao;
import br.com.fiap.alertabairro.dominio.usuario.Usuario;
import br.com.fiap.alertabairro.util.Formato;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Registro de um problema urbano feito por um usuário em um local (tabela OCORRENCIA).
 * O id funciona como número de protocolo.
 * <p>
 * Em DDD, a ocorrência é a <b>raiz do agregado</b>: fotos e histórico só existem
 * dentro dela e só mudam pelos métodos desta classe.
 * <p>
 * <b>Encapsulamento:</b> não há setter de status. A única forma de mudar a situação
 * é {@link #alterarStatus}, que confere a permissão do responsável, valida a
 * transição e grava o histórico. Assim, nenhuma ocorrência fica num estado inválido.
 */
public class Ocorrencia extends EntidadeBase {

    /** Menor gravidade aceita (leve). */
    public static final int GRAVIDADE_MINIMA = 1;
    /** Maior gravidade aceita (risco à vida). */
    public static final int GRAVIDADE_MAXIMA = 5;
    /** Quantidade máxima de fotos por ocorrência. */
    public static final int MAXIMO_FOTOS = 5;
    /** Índice de risco a partir do qual a ocorrência é crítica (ex.: gravidade 4 x peso 3). */
    public static final int INDICE_CRITICO = 12;
    /** Índice de risco a partir do qual a ocorrência pede atenção (ex.: gravidade 3 x peso 2). */
    public static final int INDICE_ATENCAO = 6;

    private final Usuario autor;
    private final Localizacao localizacao;
    private final TipoOcorrencia tipo;
    private final String descricao;
    private final int gravidade;
    private final LocalDateTime dataRegistro;
    private StatusOcorrencia status = StatusOcorrencia.ABERTA;
    private LocalDateTime dataResolucao;
    private final List<FotoOcorrencia> fotos = new ArrayList<>();
    private final List<HistoricoStatus> historico = new ArrayList<>();

    /**
     * Registra uma nova ocorrência com status ABERTA.
     *
     * @param autor        quem registra (precisa estar ativo)
     * @param localizacao  onde aconteceu
     * @param tipo         categoria do problema
     * @param descricao    relato do morador (10 a 500 caracteres)
     * @param gravidade    gravidade percebida, de 1 a 5
     * @param dataRegistro data e hora do registro
     * @throws AcessoNegadoException se o autor não pode registrar
     * @throws RegraNegocioException se algum dado for inválido
     */
    public Ocorrencia(Usuario autor, Localizacao localizacao, TipoOcorrencia tipo, String descricao,
                      int gravidade, LocalDateTime dataRegistro) {
        this.autor = Validacao.obrigatorio(autor, "Autor");
        if (!autor.temPermissao(Permissao.REGISTRAR_OCORRENCIA)) {
            throw new AcessoNegadoException(autor.getNome() + " não pode registrar ocorrências (conta inativa).");
        }
        this.localizacao = Validacao.obrigatorio(localizacao, "Localização");
        this.tipo = Validacao.obrigatorio(tipo, "Tipo de ocorrência");
        this.descricao = Validacao.texto(descricao, "Descrição", 10, 500);
        this.gravidade = Validacao.intervalo(gravidade, "Gravidade", GRAVIDADE_MINIMA, GRAVIDADE_MAXIMA);
        this.dataRegistro = Validacao.obrigatorio(dataRegistro, "Data de registro");
    }

    /**
     * Anexa uma foto. Só é possível enquanto a ocorrência está ativa.
     *
     * @param caminhoArquivo caminho do arquivo da foto
     * @param legenda        descrição curta, ou {@code null}
     * @param quando         data e hora do envio
     * @return a foto criada
     * @throws RegraNegocioException se a ocorrência já foi encerrada ou atingiu o limite de fotos
     */
    public FotoOcorrencia adicionarFoto(String caminhoArquivo, String legenda, LocalDateTime quando) {
        if (!status.isAtiva()) {
            throw new RegraNegocioException("Não é possível anexar fotos a uma ocorrência "
                    + status.getDescricao().toLowerCase() + ".");
        }
        if (fotos.size() >= MAXIMO_FOTOS) {
            throw new RegraNegocioException("Limite de " + MAXIMO_FOTOS + " fotos por ocorrência atingido.");
        }
        FotoOcorrencia foto = new FotoOcorrencia(this, caminhoArquivo, legenda, quando);
        fotos.add(foto);
        return foto;
    }

    /**
     * Muda o status da ocorrência, registrando o histórico.
     * <p>
     * Regras: o responsável precisa da permissão {@link Permissao#ATUALIZAR_STATUS}
     * (ou {@link Permissao#DESCARTAR_OCORRENCIA} para descartar), e a transição
     * precisa ser permitida por {@link StatusOcorrencia#podeIrPara}.
     *
     * @param novo        status de destino
     * @param responsavel quem faz a mudança
     * @param observacao  justificativa, ou {@code null}
     * @param quando      data e hora da mudança
     * @return o registro de histórico criado
     * @throws AcessoNegadoException se o responsável não tem permissão
     * @throws RegraNegocioException se a transição não é permitida
     */
    public HistoricoStatus alterarStatus(StatusOcorrencia novo, Usuario responsavel, String observacao,
                                         LocalDateTime quando) {
        Validacao.obrigatorio(novo, "Novo status");
        Validacao.obrigatorio(responsavel, "Responsável");
        Validacao.obrigatorio(quando, "Data da alteração");

        Permissao necessaria = novo == StatusOcorrencia.DESCARTADA
                ? Permissao.DESCARTAR_OCORRENCIA
                : Permissao.ATUALIZAR_STATUS;
        if (!responsavel.temPermissao(necessaria)) {
            throw new AcessoNegadoException(String.format(
                    "%s (%s) não tem permissão para mudar a ocorrência #%d para \"%s\".",
                    responsavel.getNome(), responsavel.getPerfil().getDescricao(), getId(), novo.getDescricao()));
        }
        if (!status.podeIrPara(novo)) {
            throw new RegraNegocioException(String.format(
                    "Transição inválida na ocorrência #%d: \"%s\" -> \"%s\". A partir de \"%s\" só é possível: %s.",
                    getId(), status.getDescricao(), novo.getDescricao(), status.getDescricao(),
                    status.proximosPermitidos().isEmpty() ? "nenhuma mudança (status final)"
                            : status.proximosPermitidos().stream().map(StatusOcorrencia::getDescricao).toList()));
        }
        if (quando.isBefore(dataRegistro)) {
            throw new RegraNegocioException("A alteração não pode ser anterior ao registro da ocorrência.");
        }

        HistoricoStatus registro = new HistoricoStatus(this, status, novo, observacao, responsavel, quando);
        historico.add(registro);
        status = novo;
        if (novo == StatusOcorrencia.RESOLVIDA) {
            dataResolucao = quando;
        }
        return registro;
    }

    /**
     * Índice de risco = gravidade x peso do tipo. Vai de 1 a 15.
     *
     * @return índice de risco
     */
    public int calcularIndiceRisco() {
        return gravidade * tipo.getPesoRisco();
    }

    /**
     * Classifica a ocorrência pelo índice de risco.
     *
     * @return CRITICO (12 ou mais), ATENCAO (6 a 11) ou NORMAL (até 5)
     */
    public NivelRisco classificarRisco() {
        int indice = calcularIndiceRisco();
        if (indice >= INDICE_CRITICO) {
            return NivelRisco.CRITICO;
        }
        if (indice >= INDICE_ATENCAO) {
            return NivelRisco.ATENCAO;
        }
        return NivelRisco.NORMAL;
    }

    /**
     * Verifica se a ocorrência foi registrada dentro de um período.
     *
     * @param inicio início do período (inclusive)
     * @param fim    fim do período (exclusive)
     * @return {@code true} se o registro está no período
     */
    public boolean registradaEntre(LocalDateTime inicio, LocalDateTime fim) {
        return !dataRegistro.isBefore(inicio) && dataRegistro.isBefore(fim);
    }

    /**
     * Retorna {@code true} enquanto precisa de acompanhamento.
     *
     * @return {@code true} enquanto precisa de acompanhamento
     */
    public boolean isAtiva() {
        return status.isAtiva();
    }

    /**
     * Retorna bairro onde a ocorrência aconteceu (vem da localização).
     *
     * @return bairro onde a ocorrência aconteceu (vem da localização)
     */
    public Bairro getBairro() {
        return localizacao.getBairro();
    }

    /**
     * Retorna quem registrou.
     *
     * @return quem registrou
     */
    public Usuario getAutor() {
        return autor;
    }

    /**
     * Retorna onde aconteceu.
     *
     * @return onde aconteceu
     */
    public Localizacao getLocalizacao() {
        return localizacao;
    }

    /**
     * Retorna categoria do problema.
     *
     * @return categoria do problema
     */
    public TipoOcorrencia getTipo() {
        return tipo;
    }

    /**
     * Retorna relato do morador.
     *
     * @return relato do morador
     */
    public String getDescricao() {
        return descricao;
    }

    /**
     * Retorna gravidade de 1 a 5.
     *
     * @return gravidade de 1 a 5
     */
    public int getGravidade() {
        return gravidade;
    }

    /**
     * Retorna data e hora do registro.
     *
     * @return data e hora do registro
     */
    public LocalDateTime getDataRegistro() {
        return dataRegistro;
    }

    /**
     * Retorna status atual.
     *
     * @return status atual
     */
    public StatusOcorrencia getStatus() {
        return status;
    }

    /**
     * Retorna data e hora da resolução, ou {@code null}.
     *
     * @return data e hora da resolução, ou {@code null}
     */
    public LocalDateTime getDataResolucao() {
        return dataResolucao;
    }

    /**
     * Retorna fotos anexadas (somente leitura).
     *
     * @return fotos anexadas (somente leitura)
     */
    public List<FotoOcorrencia> getFotos() {
        return Collections.unmodifiableList(fotos);
    }

    /**
     * Retorna histórico de mudanças de status, em ordem (somente leitura).
     *
     * @return histórico de mudanças de status, em ordem (somente leitura)
     */
    public List<HistoricoStatus> getHistorico() {
        return Collections.unmodifiableList(historico);
    }

    @Override
    public String resumo() {
        return String.format("#%d %s | %s | gravidade %d | risco %d (%s) | %s | %s",
                getId(), tipo.getNome(), localizacao.getDescricaoCurta(), gravidade,
                calcularIndiceRisco(), classificarRisco().getDescricao(), status.getDescricao(),
                Formato.diaHora(dataRegistro));
    }
}

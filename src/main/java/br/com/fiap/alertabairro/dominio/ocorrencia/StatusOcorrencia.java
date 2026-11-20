package br.com.fiap.alertabairro.dominio.ocorrencia;

import java.util.Arrays;
import java.util.List;

/**
 * Situação de uma ocorrência e as mudanças permitidas entre elas.
 * Os nomes das constantes são os valores aceitos na coluna {@code st_ocorrencia}.
 *
 * <pre>
 * ABERTA ──► EM_ANALISE ──► EM_ATENDIMENTO ──► RESOLVIDA
 *    │           │  └──────────────────────────► RESOLVIDA
 *    └───────────┴──► DESCARTADA
 * </pre>
 */
public enum StatusOcorrencia {

    /** Registrada pelo morador, ainda não conferida. */
    ABERTA("Aberta"),
    /** Conferida por um agente ou administrador. */
    EM_ANALISE("Em análise"),
    /** Encaminhada para quem pode resolver (zeladoria, Defesa Civil etc.). */
    EM_ATENDIMENTO("Em atendimento"),
    /** Problema resolvido. Status final. */
    RESOLVIDA("Resolvida"),
    /** Registro improcedente, duplicado ou de teste. Status final. */
    DESCARTADA("Descartada");

    private final String descricao;

    StatusOcorrencia(String descricao) {
        this.descricao = descricao;
    }

    /**
     * Retorna nome amigável do status.
     *
     * @return nome amigável do status
     */
    public String getDescricao() {
        return descricao;
    }

    /**
     * Diz se é possível sair deste status e ir para o novo.
     *
     * @param novo status de destino
     * @return {@code true} se a transição é permitida
     */
    public boolean podeIrPara(StatusOcorrencia novo) {
        return switch (this) {
            case ABERTA -> novo == EM_ANALISE || novo == DESCARTADA;
            case EM_ANALISE -> novo == EM_ATENDIMENTO || novo == RESOLVIDA || novo == DESCARTADA;
            case EM_ATENDIMENTO -> novo == RESOLVIDA;
            case RESOLVIDA, DESCARTADA -> false;
        };
    }

    /**
     * Retorna lista dos status para os quais se pode ir a partir deste.
     *
     * @return lista dos status para os quais se pode ir a partir deste
     */
    public List<StatusOcorrencia> proximosPermitidos() {
        return Arrays.stream(values()).filter(this::podeIrPara).toList();
    }

    /**
     * Retorna {@code true} enquanto a ocorrência ainda precisa de acompanhamento.
     *
     * @return {@code true} enquanto a ocorrência ainda precisa de acompanhamento
     */
    public boolean isAtiva() {
        return this == ABERTA || this == EM_ANALISE || this == EM_ATENDIMENTO;
    }
}

package br.com.fiap.alertabairro.dominio;

/**
 * Nível de risco usado em todo o domínio: na classificação das ocorrências,
 * nas leituras dos sensores e nos alertas.
 * <p>
 * A ordem das constantes importa: NORMAL &lt; ATENCAO &lt; CRITICO.
 */
public enum NivelRisco {

    /** Situação sem risco relevante. */
    NORMAL("Normal"),
    /** Situação que pede acompanhamento. */
    ATENCAO("Atenção"),
    /** Situação de risco alto; evitar o local. */
    CRITICO("Crítico");

    private final String descricao;

    NivelRisco(String descricao) {
        this.descricao = descricao;
    }

    /**
     * Texto amigável para exibir ao usuário.
     *
     * @return por exemplo "Atenção"
     */
    public String getDescricao() {
        return descricao;
    }

    /**
     * Indica se o nível justifica a emissão de um alerta.
     *
     * @return {@code true} para ATENCAO e CRITICO
     */
    public boolean exigeAlerta() {
        return this != NORMAL;
    }

    /**
     * Compara a gravidade de dois níveis.
     *
     * @param outro nível de referência
     * @return {@code true} se este nível é igual ou mais grave que o outro
     */
    public boolean isPeloMenos(NivelRisco outro) {
        return this.ordinal() >= outro.ordinal();
    }
}

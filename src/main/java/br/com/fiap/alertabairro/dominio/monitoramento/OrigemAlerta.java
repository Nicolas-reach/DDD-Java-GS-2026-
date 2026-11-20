package br.com.fiap.alertabairro.dominio.monitoramento;

/**
 * O que gerou um alerta. Os nomes são os valores da coluna {@code tp_origem}.
 */
public enum OrigemAlerta {

    /** Uma leitura de sensor passou do limite. */
    SENSOR("Leitura de sensor"),
    /** Muitas ocorrências de risco no mesmo bairro em pouco tempo. */
    OCORRENCIAS("Concentração de ocorrências");

    private final String descricao;

    OrigemAlerta(String descricao) {
        this.descricao = descricao;
    }

    /**
     * Retorna nome amigável da origem.
     *
     * @return nome amigável da origem
     */
    public String getDescricao() {
        return descricao;
    }
}

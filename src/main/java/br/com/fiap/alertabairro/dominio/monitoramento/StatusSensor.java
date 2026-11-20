package br.com.fiap.alertabairro.dominio.monitoramento;

/**
 * Situação operacional de um sensor. Os nomes são os valores da coluna {@code st_sensor}.
 */
public enum StatusSensor {

    /** Funcionando e enviando leituras. */
    ATIVO("ativo"),
    /** Desligado ou retirado. */
    INATIVO("inativo"),
    /** Em manutenção; leituras são recusadas. */
    MANUTENCAO("em manutenção");

    private final String descricao;

    StatusSensor(String descricao) {
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
}

package br.com.fiap.alertabairro.servico;

/**
 * Estatísticas das leituras de um sensor (resultado de consulta, sem identidade própria).
 *
 * @param codigoSerie      número de série do sensor
 * @param unidade          unidade das leituras
 * @param quantidade       total de leituras
 * @param minimo           menor valor lido
 * @param maximo           maior valor lido
 * @param media            média das leituras
 * @param leiturasEmAlerta leituras classificadas como atenção ou crítico
 */
public record ResumoLeituras(String codigoSerie, String unidade, long quantidade, double minimo, double maximo,
                             double media, long leiturasEmAlerta) {
}

package br.com.fiap.alertabairro.dominio.monitoramento;

import br.com.fiap.alertabairro.dominio.EntidadeBase;
import br.com.fiap.alertabairro.dominio.NivelRisco;
import br.com.fiap.alertabairro.util.Formato;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Leitura feita por um sensor em um instante (tabela MEDICAO).
 * <p>
 * A unidade não fica na medição: ela vem do tipo do sensor ({@link Sensor#getUnidade()}),
 * assim como no banco. O nível de risco é calculado pelo próprio sensor no momento da leitura.
 * <p>
 * Só {@link Sensor#registrarLeitura} cria medições (construtor com visibilidade de pacote).
 */
public class Medicao extends EntidadeBase {

    private final Sensor sensor;
    private final double valor;
    private final LocalDateTime dataMedicao;
    private final NivelRisco nivelRisco;
    private final Double variacao;

    Medicao(Sensor sensor, double valor, LocalDateTime dataMedicao, NivelRisco nivelRisco, Double variacao) {
        this.sensor = sensor;
        this.valor = valor;
        this.dataMedicao = dataMedicao;
        this.nivelRisco = nivelRisco;
        this.variacao = variacao;
    }

    /**
     * Retorna sensor que fez a leitura.
     *
     * @return sensor que fez a leitura
     */
    public Sensor getSensor() {
        return sensor;
    }

    /**
     * Retorna valor lido, na unidade do sensor.
     *
     * @return valor lido, na unidade do sensor
     */
    public double getValor() {
        return valor;
    }

    /**
     * Retorna data e hora da leitura.
     *
     * @return data e hora da leitura
     */
    public LocalDateTime getDataMedicao() {
        return dataMedicao;
    }

    /**
     * Retorna nível de risco atribuído pelo sensor.
     *
     * @return nível de risco atribuído pelo sensor
     */
    public NivelRisco getNivelRisco() {
        return nivelRisco;
    }

    /**
     * Retorna diferença em relação à leitura anterior do mesmo sensor, se houver.
     *
     * @return diferença em relação à leitura anterior do mesmo sensor, se houver
     */
    public Optional<Double> getVariacao() {
        return Optional.ofNullable(variacao);
    }

    /**
     * Retorna descrição da leitura no vocabulário do tipo de sensor.
     *
     * @return descrição da leitura no vocabulário do tipo de sensor
     */
    public String descrever() {
        return sensor.descreverMedicao(this);
    }

    @Override
    public String resumo() {
        return String.format("%s  %s: %s %s -> %s",
                Formato.diaHora(dataMedicao), sensor.getCodigoSerie(), Formato.numero(valor),
                sensor.getUnidade(), nivelRisco.getDescricao());
    }
}

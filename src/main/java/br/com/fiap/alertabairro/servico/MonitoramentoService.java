package br.com.fiap.alertabairro.servico;

import br.com.fiap.alertabairro.dominio.excecao.RecursoNaoEncontradoException;
import br.com.fiap.alertabairro.dominio.monitoramento.Medicao;
import br.com.fiap.alertabairro.dominio.monitoramento.Sensor;
import br.com.fiap.alertabairro.repositorio.Repositorio;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.DoubleSummaryStatistics;
import java.util.List;

/**
 * Casos de uso do módulo de monitoramento: receber leituras dos sensores e
 * resumir os dados de chuva e nível de água.
 */
public class MonitoramentoService {

    private final Repositorio<Sensor> sensores;
    private final AlertaService alertaService;
    private final Clock relogio;

    /**
     * Cria o serviço.
     *
     * @param sensores      repositório de sensores
     * @param alertaService serviço avisado a cada leitura
     * @param relogio       relógio do sistema
     */
    public MonitoramentoService(Repositorio<Sensor> sensores, AlertaService alertaService, Clock relogio) {
        this.sensores = sensores;
        this.alertaService = alertaService;
        this.relogio = relogio;
    }

    /**
     * Recebe uma leitura identificando o sensor pelo id.
     *
     * @param idSensor id do sensor
     * @param valor    valor lido
     * @return a medição registrada
     */
    public Medicao registrarMedicao(long idSensor, double valor) {
        return registrar(sensores.buscarPorIdOuFalhar(idSensor), valor);
    }

    /**
     * Recebe uma leitura identificando o sensor pelo número de série, como faria
     * o equipamento real (sobrecarga do método anterior).
     *
     * @param codigoSerie número de série do sensor
     * @param valor       valor lido
     * @return a medição registrada
     */
    public Medicao registrarMedicao(String codigoSerie, double valor) {
        return registrar(buscarPorSerie(codigoSerie), valor);
    }

    private Medicao registrar(Sensor sensor, double valor) {
        Medicao medicao = sensor.registrarLeitura(valor, LocalDateTime.now(relogio));
        alertaService.emitirPorMedicao(medicao);
        return medicao;
    }

    /**
     * Busca um sensor pelo número de série.
     *
     * @param codigoSerie número de série
     * @return o sensor
     * @throws RecursoNaoEncontradoException se não existir
     */
    public Sensor buscarPorSerie(String codigoSerie) {
        return sensores.filtrar(s -> s.getCodigoSerie().equalsIgnoreCase(codigoSerie)).stream()
                .findFirst()
                .orElseThrow(() -> new RecursoNaoEncontradoException("Sensor", codigoSerie));
    }

    /**
     * Calcula mínimo, máximo e média das leituras de um sensor.
     *
     * @param sensor sensor
     * @return resumo das leituras
     */
    public ResumoLeituras resumirLeituras(Sensor sensor) {
        DoubleSummaryStatistics estatisticas = sensor.getMedicoes().stream()
                .mapToDouble(Medicao::getValor)
                .summaryStatistics();
        long emAlerta = sensor.getMedicoes().stream().filter(m -> m.getNivelRisco().exigeAlerta()).count();
        if (estatisticas.getCount() == 0) {
            return new ResumoLeituras(sensor.getCodigoSerie(), sensor.getUnidade(), 0, 0, 0, 0, 0);
        }
        return new ResumoLeituras(sensor.getCodigoSerie(), sensor.getUnidade(), estatisticas.getCount(),
                estatisticas.getMin(), estatisticas.getMax(), estatisticas.getAverage(), emAlerta);
    }

    /**
     * Retorna todos os sensores.
     *
     * @return todos os sensores
     */
    public List<Sensor> listarSensores() {
        return sensores.listarTodos();
    }
}

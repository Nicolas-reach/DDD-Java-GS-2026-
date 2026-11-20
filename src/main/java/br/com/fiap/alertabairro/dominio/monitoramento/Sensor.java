package br.com.fiap.alertabairro.dominio.monitoramento;

import br.com.fiap.alertabairro.dominio.EntidadeBase;
import br.com.fiap.alertabairro.dominio.NivelRisco;
import br.com.fiap.alertabairro.dominio.Validacao;
import br.com.fiap.alertabairro.dominio.excecao.RegraNegocioException;
import br.com.fiap.alertabairro.dominio.territorio.Bairro;
import br.com.fiap.alertabairro.dominio.territorio.Localizacao;
import br.com.fiap.alertabairro.util.Formato;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Equipamento (simulado) instalado em um ponto do bairro (tabela SENSOR).
 * <p>
 * <b>Abstração e herança:</b> a classe é abstrata e tem duas especializações,
 * {@link SensorNivelAgua} e {@link Pluviometro}, que no banco se diferenciam
 * pela coluna {@code tp_sensor}.
 * <p>
 * <b>Polimorfismo:</b> {@link #registrarLeitura} é o mesmo para todos (método
 * {@code final}, padrão Template Method), mas cada subclasse decide como
 * {@link #classificar classificar} a leitura, qual é a unidade e qual é a faixa
 * plausível de valores.
 */
public abstract class Sensor extends EntidadeBase {

    private final String codigoSerie;
    private final Localizacao localizacao;
    private final double limiteAtencao;
    private final double limiteCritico;
    private final LocalDate dataInstalacao;
    private StatusSensor status = StatusSensor.ATIVO;
    private final List<Medicao> medicoes = new ArrayList<>();

    /**
     * Construtor usado pelas subclasses.
     *
     * @param codigoSerie    número de série do equipamento
     * @param localizacao    ponto onde está instalado
     * @param limiteAtencao  valor a partir do qual a leitura pede atenção (maior que zero)
     * @param limiteCritico  valor a partir do qual a leitura é crítica (maior que o de atenção)
     * @param dataInstalacao data de instalação
     */
    protected Sensor(String codigoSerie, Localizacao localizacao, double limiteAtencao, double limiteCritico,
                     LocalDate dataInstalacao) {
        this.codigoSerie = Validacao.texto(codigoSerie, "Código de série", 3, 30).toUpperCase();
        this.localizacao = Validacao.obrigatorio(localizacao, "Localização do sensor");
        if (limiteAtencao <= 0 || limiteCritico <= limiteAtencao) {
            throw new RegraNegocioException(
                    "Limites inválidos: o de atenção deve ser maior que zero e menor que o crítico.");
        }
        this.limiteAtencao = limiteAtencao;
        this.limiteCritico = limiteCritico;
        this.dataInstalacao = Validacao.obrigatorio(dataInstalacao, "Data de instalação");
    }

    /**
     * Registra uma leitura: valida, classifica e guarda.
     * <p>
     * É {@code final} para que nenhuma subclasse pule as validações. A parte que
     * muda de um tipo para outro fica em {@link #classificar}.
     *
     * @param valor   valor lido, na unidade do sensor
     * @param instante data e hora da leitura
     * @return a medição criada, já com o nível de risco
     * @throws RegraNegocioException se o sensor não está ativo, o valor é implausível
     *                               ou a leitura é anterior à última registrada
     */
    public final Medicao registrarLeitura(double valor, LocalDateTime instante) {
        Validacao.obrigatorio(instante, "Instante da leitura");
        if (status != StatusSensor.ATIVO) {
            throw new RegraNegocioException("O sensor " + codigoSerie + " está " + status.getDescricao()
                    + " e não aceita leituras.");
        }
        if (Double.isNaN(valor) || valor < 0 || valor > getValorMaximoPlausivel()) {
            throw new RegraNegocioException(String.format(
                    "Leitura de %s %s fora da faixa plausível (0 a %s %s). Possível falha do sensor %s.",
                    Formato.numero(valor), getUnidade(), Formato.numero(getValorMaximoPlausivel()),
                    getUnidade(), codigoSerie));
        }
        Medicao anterior = getUltimaMedicao().orElse(null);
        if (anterior != null && instante.isBefore(anterior.getDataMedicao())) {
            throw new RegraNegocioException("Leitura fora de ordem: anterior à última leitura do sensor "
                    + codigoSerie + ".");
        }
        NivelRisco nivel = classificar(valor, anterior, instante);
        Double variacao = anterior == null ? null : valor - anterior.getValor();
        Medicao medicao = new Medicao(this, valor, instante, nivel, variacao);
        medicoes.add(medicao);
        return medicao;
    }

    /**
     * Regra de classificação de cada tipo de sensor.
     *
     * @param valor    valor lido
     * @param anterior leitura anterior, ou {@code null} se for a primeira
     * @param instante momento da leitura
     * @return nível de risco da leitura
     */
    protected abstract NivelRisco classificar(double valor, Medicao anterior, LocalDateTime instante);

    /**
     * Classificação básica pelos limites cadastrados, reaproveitada pelas subclasses.
     *
     * @param valor valor lido
     * @return CRITICO, ATENCAO ou NORMAL
     */
    protected NivelRisco classificarPorLimites(double valor) {
        if (valor >= limiteCritico) {
            return NivelRisco.CRITICO;
        }
        if (valor >= limiteAtencao) {
            return NivelRisco.ATENCAO;
        }
        return NivelRisco.NORMAL;
    }

    /**
     * Retorna código do tipo, igual ao valor da coluna {@code tp_sensor}.
     *
     * @return código do tipo, igual ao valor da coluna {@code tp_sensor}
     */
    public abstract String getCodigoTipo();

    /**
     * Retorna nome do tipo para exibição.
     *
     * @return nome do tipo para exibição
     */
    public abstract String getTipoDescricao();

    /**
     * Retorna unidade de medida (cm, mm/h...).
     *
     * @return unidade de medida (cm, mm/h...)
     */
    public abstract String getUnidade();

    /**
     * Retorna maior valor fisicamente plausível; acima disso a leitura é tratada como falha.
     *
     * @return maior valor fisicamente plausível; acima disso a leitura é tratada como falha
     */
    protected abstract double getValorMaximoPlausivel();

    /**
     * Descreve uma medição com o vocabulário do tipo de sensor, para alertas e painéis.
     *
     * @param medicao medição deste sensor
     * @return frase curta
     */
    public abstract String descreverMedicao(Medicao medicao);

    /** Coloca o sensor em manutenção: leituras passam a ser recusadas. */
    public void colocarEmManutencao() {
        status = StatusSensor.MANUTENCAO;
    }

    /** Volta o sensor a funcionar. */
    public void ativar() {
        status = StatusSensor.ATIVO;
    }

    /** Desliga o sensor. */
    public void desativar() {
        status = StatusSensor.INATIVO;
    }

    /**
     * Retorna última leitura, se houver.
     *
     * @return última leitura, se houver
     */
    public Optional<Medicao> getUltimaMedicao() {
        return medicoes.isEmpty() ? Optional.empty() : Optional.of(medicoes.get(medicoes.size() - 1));
    }

    /**
     * Retorna todas as leituras, em ordem (somente leitura).
     *
     * @return todas as leituras, em ordem (somente leitura)
     */
    public List<Medicao> getMedicoes() {
        return Collections.unmodifiableList(medicoes);
    }

    /**
     * Retorna bairro onde o sensor está.
     *
     * @return bairro onde o sensor está
     */
    public Bairro getBairro() {
        return localizacao.getBairro();
    }

    /**
     * Retorna número de série.
     *
     * @return número de série
     */
    public String getCodigoSerie() {
        return codigoSerie;
    }

    /**
     * Retorna ponto de instalação.
     *
     * @return ponto de instalação
     */
    public Localizacao getLocalizacao() {
        return localizacao;
    }

    /**
     * Retorna limite de atenção.
     *
     * @return limite de atenção
     */
    public double getLimiteAtencao() {
        return limiteAtencao;
    }

    /**
     * Retorna limite crítico.
     *
     * @return limite crítico
     */
    public double getLimiteCritico() {
        return limiteCritico;
    }

    /**
     * Retorna data de instalação.
     *
     * @return data de instalação
     */
    public LocalDate getDataInstalacao() {
        return dataInstalacao;
    }

    /**
     * Retorna status operacional.
     *
     * @return status operacional
     */
    public StatusSensor getStatus() {
        return status;
    }

    @Override
    public String resumo() {
        return String.format("%s | %s | %s | atenção %s / crítico %s %s | %s",
                codigoSerie, getTipoDescricao(), localizacao.getDescricaoCurta(),
                Formato.numero(limiteAtencao), Formato.numero(limiteCritico), getUnidade(), status.getDescricao());
    }
}

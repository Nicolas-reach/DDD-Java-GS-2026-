package br.com.fiap.alertabairro.dominio.monitoramento;

import br.com.fiap.alertabairro.dominio.NivelRisco;
import br.com.fiap.alertabairro.dominio.territorio.Localizacao;
import br.com.fiap.alertabairro.util.Formato;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Sensor de nível de água de córrego, em centímetros.
 * <p>
 * Além dos limites fixos, considera a <b>velocidade da subida</b>: se o nível subir
 * muito em pouco tempo, a leitura vira ATENÇÃO mesmo abaixo do limite, porque a
 * água pode chegar às casas antes da próxima leitura.
 */
public class SensorNivelAgua extends Sensor {

    /** Subida que caracteriza enchente rápida, se ocorrer dentro de {@link #JANELA_SUBIDA}. */
    public static final double SUBIDA_RAPIDA_PADRAO_CM = 40.0;
    /** Intervalo máximo entre duas leituras para considerar a subida como rápida. */
    public static final Duration JANELA_SUBIDA = Duration.ofHours(1);
    private static final double NIVEL_MAXIMO_PLAUSIVEL_CM = 600.0;

    private final double subidaRapidaCm;

    /**
     * Cria um sensor de nível com o critério padrão de subida rápida (40 cm em até 1 h).
     *
     * @param codigoSerie    número de série
     * @param localizacao    ponto de instalação
     * @param limiteAtencao  nível de atenção em cm
     * @param limiteCritico  nível crítico em cm
     * @param dataInstalacao data de instalação
     */
    public SensorNivelAgua(String codigoSerie, Localizacao localizacao, double limiteAtencao,
                           double limiteCritico, LocalDate dataInstalacao) {
        this(codigoSerie, localizacao, limiteAtencao, limiteCritico, dataInstalacao, SUBIDA_RAPIDA_PADRAO_CM);
    }

    /**
     * Cria um sensor de nível com critério próprio de subida rápida (sobrecarga de construtor).
     *
     * @param codigoSerie    número de série
     * @param localizacao    ponto de instalação
     * @param limiteAtencao  nível de atenção em cm
     * @param limiteCritico  nível crítico em cm
     * @param dataInstalacao data de instalação
     * @param subidaRapidaCm subida em cm que, dentro de 1 h, gera atenção
     */
    public SensorNivelAgua(String codigoSerie, Localizacao localizacao, double limiteAtencao,
                           double limiteCritico, LocalDate dataInstalacao, double subidaRapidaCm) {
        super(codigoSerie, localizacao, limiteAtencao, limiteCritico, dataInstalacao);
        this.subidaRapidaCm = subidaRapidaCm;
    }

    @Override
    protected NivelRisco classificar(double valor, Medicao anterior, LocalDateTime instante) {
        NivelRisco porLimite = classificarPorLimites(valor);
        if (porLimite == NivelRisco.NORMAL && subiuRapido(valor, anterior, instante)) {
            return NivelRisco.ATENCAO;
        }
        return porLimite;
    }

    private boolean subiuRapido(double valor, Medicao anterior, LocalDateTime instante) {
        if (anterior == null) {
            return false;
        }
        boolean dentroDaJanela = Duration.between(anterior.getDataMedicao(), instante)
                .compareTo(JANELA_SUBIDA) <= 0;
        return dentroDaJanela && valor - anterior.getValor() >= subidaRapidaCm;
    }

    @Override
    public String getCodigoTipo() {
        return "NIVEL_AGUA";
    }

    @Override
    public String getTipoDescricao() {
        return "Sensor de nível de água";
    }

    @Override
    public String getUnidade() {
        return "cm";
    }

    @Override
    protected double getValorMaximoPlausivel() {
        return NIVEL_MAXIMO_PLAUSIVEL_CM;
    }

    @Override
    public String descreverMedicao(Medicao medicao) {
        double valor = medicao.getValor();
        String base = "Nível do córrego em " + Formato.numero(valor) + " cm";
        if (valor >= getLimiteCritico()) {
            return base + ", acima do limite crítico de " + Formato.numero(getLimiteCritico()) + " cm";
        }
        if (valor >= getLimiteAtencao()) {
            return base + ", acima do limite de atenção de " + Formato.numero(getLimiteAtencao()) + " cm";
        }
        if (medicao.getNivelRisco() == NivelRisco.ATENCAO) {
            double subida = medicao.getVariacao().orElse(0.0);
            return base + ", subindo rápido (+" + Formato.numero(subida) + " cm em menos de 1 h)";
        }
        return base + ", dentro do normal";
    }
}

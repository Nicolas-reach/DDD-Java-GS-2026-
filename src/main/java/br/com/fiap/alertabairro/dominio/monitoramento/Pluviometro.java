package br.com.fiap.alertabairro.dominio.monitoramento;

import br.com.fiap.alertabairro.dominio.NivelRisco;
import br.com.fiap.alertabairro.dominio.territorio.Localizacao;
import br.com.fiap.alertabairro.util.Formato;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Pluviômetro: mede a intensidade da chuva em milímetros por hora.
 * <p>
 * Diferente do sensor de nível, a classificação usa só a intensidade do momento,
 * comparada aos limites cadastrados. A descrição usa a faixa de intensidade
 * (fraca, moderada, forte, muito forte), mais fácil de entender para o morador.
 */
public class Pluviometro extends Sensor {

    private static final double CHUVA_MAXIMA_PLAUSIVEL_MM_H = 300.0;

    /**
     * Cria um pluviômetro.
     *
     * @param codigoSerie    número de série
     * @param localizacao    ponto de instalação
     * @param limiteAtencao  intensidade de atenção em mm/h
     * @param limiteCritico  intensidade crítica em mm/h
     * @param dataInstalacao data de instalação
     */
    public Pluviometro(String codigoSerie, Localizacao localizacao, double limiteAtencao, double limiteCritico,
                       LocalDate dataInstalacao) {
        super(codigoSerie, localizacao, limiteAtencao, limiteCritico, dataInstalacao);
    }

    @Override
    protected NivelRisco classificar(double valor, Medicao anterior, LocalDateTime instante) {
        return classificarPorLimites(valor);
    }

    /**
     * Faixa de intensidade da chuva.
     *
     * @param mmPorHora intensidade em mm/h
     * @return "sem chuva", "fraca", "moderada", "forte" ou "muito forte"
     */
    public static String classificarIntensidade(double mmPorHora) {
        if (mmPorHora <= 0) {
            return "sem chuva";
        }
        if (mmPorHora <= 5) {
            return "fraca";
        }
        if (mmPorHora <= 25) {
            return "moderada";
        }
        if (mmPorHora <= 50) {
            return "forte";
        }
        return "muito forte";
    }

    @Override
    public String getCodigoTipo() {
        return "PLUVIOMETRO";
    }

    @Override
    public String getTipoDescricao() {
        return "Pluviômetro";
    }

    @Override
    public String getUnidade() {
        return "mm/h";
    }

    @Override
    protected double getValorMaximoPlausivel() {
        return CHUVA_MAXIMA_PLAUSIVEL_MM_H;
    }

    @Override
    public String descreverMedicao(Medicao medicao) {
        double valor = medicao.getValor();
        return "Chuva " + classificarIntensidade(valor) + " de " + Formato.numero(valor) + " mm/h";
    }
}

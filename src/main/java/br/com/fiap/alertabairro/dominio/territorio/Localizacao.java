package br.com.fiap.alertabairro.dominio.territorio;

import br.com.fiap.alertabairro.dominio.EntidadeBase;
import br.com.fiap.alertabairro.dominio.Validacao;

/**
 * Ponto físico dentro de um bairro: rua, referência e coordenadas (tabela LOCALIZACAO).
 * <p>
 * Exemplos: a esquina que sempre alaga, a ponte sobre o córrego, a encosta atrás da UBS.
 * Várias ocorrências no mesmo ponto revelam um problema recorrente.
 */
public class Localizacao extends EntidadeBase {

    private final String logradouro;
    private final String referencia;
    private final double latitude;
    private final double longitude;
    private final Bairro bairro;

    /**
     * Cria um ponto monitorado.
     *
     * @param logradouro rua, avenida ou travessa (até 120 caracteres)
     * @param referencia referência reconhecida pelo morador, ou {@code null}
     * @param latitude   latitude entre -90 e 90
     * @param longitude  longitude entre -180 e 180
     * @param bairro     bairro onde o ponto fica (obrigatório)
     */
    public Localizacao(String logradouro, String referencia, double latitude, double longitude, Bairro bairro) {
        this.logradouro = Validacao.texto(logradouro, "Logradouro", 3, 120);
        this.referencia = Validacao.textoOpcional(referencia, "Referência", 150);
        this.latitude = Validacao.intervalo(latitude, "Latitude", -90, 90);
        this.longitude = Validacao.intervalo(longitude, "Longitude", -180, 180);
        this.bairro = Validacao.obrigatorio(bairro, "Bairro");
    }

    /**
     * Retorna logradouro.
     *
     * @return logradouro
     */
    public String getLogradouro() {
        return logradouro;
    }

    /**
     * Retorna referência, ou {@code null}.
     *
     * @return referência, ou {@code null}
     */
    public String getReferencia() {
        return referencia;
    }

    /**
     * Retorna latitude.
     *
     * @return latitude
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * Retorna longitude.
     *
     * @return longitude
     */
    public double getLongitude() {
        return longitude;
    }

    /**
     * Retorna bairro do ponto.
     *
     * @return bairro do ponto
     */
    public Bairro getBairro() {
        return bairro;
    }

    /**
     * Retorna logradouro com a referência entre parênteses, quando houver.
     *
     * @return logradouro com a referência entre parênteses, quando houver
     */
    public String getDescricaoCurta() {
        return referencia == null ? logradouro : logradouro + " (" + referencia + ")";
    }

    @Override
    public String resumo() {
        return "Local #" + getId() + " " + getDescricaoCurta() + " - " + bairro.getNome();
    }
}

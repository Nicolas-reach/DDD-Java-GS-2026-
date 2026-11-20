package br.com.fiap.alertabairro.infraestrutura;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Relógio controlado pela demonstração: começa em um horário escolhido e só avança
 * quando {@link #avancar} é chamado.
 * <p>
 * Os serviços recebem um {@link Clock} em vez de chamar {@code LocalDateTime.now()}
 * direto. Em produção usaria-se {@code Clock.systemDefaultZone()}; aqui, este relógio
 * permite simular uma tarde de chuva em poucos segundos, sempre com o mesmo resultado.
 */
public class RelogioSimulado extends Clock {

    private final ZoneId zona;
    private Instant agora;

    /**
     * Cria o relógio.
     *
     * @param inicio horário inicial da simulação
     * @param zona   fuso horário
     */
    public RelogioSimulado(LocalDateTime inicio, ZoneId zona) {
        this.zona = zona;
        this.agora = inicio.atZone(zona).toInstant();
    }

    private RelogioSimulado(Instant agora, ZoneId zona) {
        this.zona = zona;
        this.agora = agora;
    }

    /**
     * Avança o relógio.
     *
     * @param duracao quanto tempo avançar
     */
    public void avancar(Duration duracao) {
        agora = agora.plus(duracao);
    }

    /**
     * Avança o relógio em minutos.
     *
     * @param minutos quantos minutos avançar
     */
    public void avancarMinutos(long minutos) {
        avancar(Duration.ofMinutes(minutos));
    }

    /**
     * Retorna horário atual da simulação.
     *
     * @return horário atual da simulação
     */
    public LocalDateTime agora() {
        return LocalDateTime.ofInstant(agora, zona);
    }

    @Override
    public ZoneId getZone() {
        return zona;
    }

    @Override
    public Clock withZone(ZoneId novaZona) {
        return new RelogioSimulado(agora, novaZona);
    }

    @Override
    public Instant instant() {
        return agora;
    }
}

package br.com.fiap.alertabairro.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Formatação padrão (pt-BR) de datas e números usada nas mensagens do sistema.
 * <p>
 * Classe utilitária: só tem métodos estáticos e não pode ser instanciada.
 */
public final class Formato {

    /** Localidade brasileira: vírgula como separador decimal. */
    public static final Locale BRASIL = Locale.forLanguageTag("pt-BR");

    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DIA_HORA = DateTimeFormatter.ofPattern("dd/MM HH:mm");
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private Formato() {
    }

    /**
     * Formata data e hora completas.
     *
     * @param valor data e hora (pode ser {@code null})
     * @return texto no formato dd/MM/yyyy HH:mm, ou "-" se nulo
     */
    public static String dataHora(LocalDateTime valor) {
        return valor == null ? "-" : DATA_HORA.format(valor);
    }

    /**
     * Formata dia e hora, sem o ano, para listagens curtas.
     *
     * @param valor data e hora (pode ser {@code null})
     * @return texto no formato dd/MM HH:mm, ou "-" se nulo
     */
    public static String diaHora(LocalDateTime valor) {
        return valor == null ? "-" : DIA_HORA.format(valor);
    }

    /**
     * Formata uma data sem hora.
     *
     * @param valor data (pode ser {@code null})
     * @return texto no formato dd/MM/yyyy, ou "-" se nulo
     */
    public static String data(LocalDate valor) {
        return valor == null ? "-" : DATA.format(valor);
    }

    /**
     * Formata um número com uma casa decimal e vírgula.
     *
     * @param valor número
     * @return texto como "150,0"
     */
    public static String numero(double valor) {
        return String.format(BRASIL, "%.1f", valor);
    }
}

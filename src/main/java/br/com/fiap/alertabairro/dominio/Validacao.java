package br.com.fiap.alertabairro.dominio;

import br.com.fiap.alertabairro.dominio.excecao.RegraNegocioException;

/**
 * Validações reutilizadas pelos construtores e métodos das entidades.
 * <p>
 * Centralizar as regras aqui evita repetir {@code if}s e garante mensagens
 * de erro no mesmo padrão. Cada método devolve o valor já validado, para ser
 * usado direto na atribuição: {@code this.nome = Validacao.texto(...)}.
 */
public final class Validacao {

    private Validacao() {
    }

    /**
     * Garante que um valor não é nulo.
     *
     * @param valor valor recebido
     * @param campo nome do campo, para a mensagem de erro
     * @param <T>   tipo do valor
     * @return o próprio valor
     * @throws RegraNegocioException se for nulo
     */
    public static <T> T obrigatorio(T valor, String campo) {
        if (valor == null) {
            throw new RegraNegocioException(campo + " é obrigatório(a).");
        }
        return valor;
    }

    /**
     * Valida um texto obrigatório e o seu tamanho, removendo espaços das pontas.
     *
     * @param valor  texto recebido
     * @param campo  nome do campo
     * @param minimo tamanho mínimo
     * @param maximo tamanho máximo (igual ao VARCHAR2 da coluna no banco)
     * @return o texto sem espaços nas pontas
     * @throws RegraNegocioException se estiver vazio ou fora do tamanho
     */
    public static String texto(String valor, String campo, int minimo, int maximo) {
        if (valor == null || valor.isBlank()) {
            throw new RegraNegocioException(campo + " é obrigatório(a).");
        }
        String limpo = valor.strip();
        if (limpo.length() < minimo || limpo.length() > maximo) {
            throw new RegraNegocioException(String.format(
                    "%s deve ter entre %d e %d caracteres (informado: %d).", campo, minimo, maximo, limpo.length()));
        }
        return limpo;
    }

    /**
     * Valida um texto opcional: vazio vira {@code null}.
     *
     * @param valor  texto recebido
     * @param campo  nome do campo
     * @param maximo tamanho máximo
     * @return o texto sem espaços nas pontas, ou {@code null}
     * @throws RegraNegocioException se passar do tamanho máximo
     */
    public static String textoOpcional(String valor, String campo, int maximo) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return texto(valor, campo, 1, maximo);
    }

    /**
     * Valida um inteiro dentro de um intervalo fechado.
     *
     * @param valor  número recebido
     * @param campo  nome do campo
     * @param minimo menor valor aceito
     * @param maximo maior valor aceito
     * @return o próprio valor
     * @throws RegraNegocioException se estiver fora do intervalo
     */
    public static int intervalo(int valor, String campo, int minimo, int maximo) {
        if (valor < minimo || valor > maximo) {
            throw new RegraNegocioException(String.format(
                    "%s deve estar entre %d e %d (informado: %d).", campo, minimo, maximo, valor));
        }
        return valor;
    }

    /**
     * Valida um decimal dentro de um intervalo fechado.
     *
     * @param valor  número recebido
     * @param campo  nome do campo
     * @param minimo menor valor aceito
     * @param maximo maior valor aceito
     * @return o próprio valor
     * @throws RegraNegocioException se estiver fora do intervalo
     */
    public static double intervalo(double valor, String campo, double minimo, double maximo) {
        if (Double.isNaN(valor) || valor < minimo || valor > maximo) {
            throw new RegraNegocioException(String.format(
                    "%s deve estar entre %s e %s (informado: %s).", campo, minimo, maximo, valor));
        }
        return valor;
    }
}

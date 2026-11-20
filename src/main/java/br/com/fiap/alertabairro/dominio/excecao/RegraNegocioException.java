package br.com.fiap.alertabairro.dominio.excecao;

/**
 * Exceção lançada quando uma regra do negócio do AlertaBairro é violada,
 * por exemplo uma gravidade fora de 1 a 5 ou uma mudança de status inválida.
 * <p>
 * É a exceção base do domínio: {@link AcessoNegadoException} e
 * {@link RecursoNaoEncontradoException} herdam dela. Assim, um único
 * {@code catch (RegraNegocioException e)} trata todos os casos (polimorfismo).
 */
public class RegraNegocioException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Cria a exceção com uma mensagem clara para o usuário.
     *
     * @param mensagem explicação do que foi violado
     */
    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}

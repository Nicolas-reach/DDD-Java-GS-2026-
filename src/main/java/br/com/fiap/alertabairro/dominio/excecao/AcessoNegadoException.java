package br.com.fiap.alertabairro.dominio.excecao;

/**
 * Exceção lançada quando um usuário tenta uma ação que o seu perfil não permite,
 * como um morador tentando alterar o status de uma ocorrência.
 */
public class AcessoNegadoException extends RegraNegocioException {

    private static final long serialVersionUID = 1L;

    /**
     * Cria a exceção.
     *
     * @param mensagem quem tentou o quê, e por que não pode
     */
    public AcessoNegadoException(String mensagem) {
        super(mensagem);
    }
}

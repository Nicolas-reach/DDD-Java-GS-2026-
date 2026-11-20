package br.com.fiap.alertabairro.dominio.excecao;

/**
 * Exceção lançada quando se busca um registro que não existe,
 * como uma ocorrência com um número de protocolo inválido.
 */
public class RecursoNaoEncontradoException extends RegraNegocioException {

    private static final long serialVersionUID = 1L;

    /**
     * Cria a exceção a partir do tipo e do identificador procurados.
     *
     * @param entidade nome do tipo procurado (ex.: "Ocorrência")
     * @param id       identificador que não foi encontrado
     */
    public RecursoNaoEncontradoException(String entidade, Object id) {
        super("Registro não encontrado: " + entidade + " #" + id + ".");
    }
}

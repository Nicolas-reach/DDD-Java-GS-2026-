package br.com.fiap.alertabairro.repositorio;

import br.com.fiap.alertabairro.dominio.EntidadeBase;
import br.com.fiap.alertabairro.dominio.excecao.RecursoNaoEncontradoException;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Contrato de armazenamento de entidades (padrão Repository do DDD).
 * <p>
 * Os serviços dependem desta interface, não de uma implementação. Hoje o projeto
 * usa {@link RepositorioMemoria}; uma implementação com JDBC no Oracle, usando as
 * tabelas do Database Design, pode substituí-la sem mudar nenhum serviço.
 *
 * @param <T> tipo de entidade armazenada
 */
public interface Repositorio<T extends EntidadeBase> {

    /**
     * Salva a entidade. Se ainda não tem id, o repositório gera um.
     *
     * @param entidade entidade a salvar
     * @return a mesma entidade, já com id
     */
    T salvar(T entidade);

    /**
     * Busca pelo identificador.
     *
     * @param id identificador
     * @return a entidade, se existir
     */
    Optional<T> buscarPorId(long id);

    /**
     * Lista tudo, na ordem em que foi salvo.
     *
     * @return lista somente leitura
     */
    List<T> listarTodos();

    /**
     * Nome da entidade, usado nas mensagens de erro.
     *
     * @return por exemplo "Ocorrência"
     */
    String getNomeEntidade();

    /**
     * Busca pelo identificador ou lança exceção se não existir.
     *
     * @param id identificador
     * @return a entidade
     * @throws RecursoNaoEncontradoException se não existir
     */
    default T buscarPorIdOuFalhar(long id) {
        return buscarPorId(id).orElseThrow(() -> new RecursoNaoEncontradoException(getNomeEntidade(), id));
    }

    /**
     * Filtra as entidades por um critério.
     *
     * @param criterio condição que a entidade precisa atender
     * @return lista das que atendem
     */
    default List<T> filtrar(Predicate<T> criterio) {
        return listarTodos().stream().filter(criterio).toList();
    }

    /**
     * Quantidade de entidades armazenadas.
     *
     * @return total
     */
    default int contar() {
        return listarTodos().size();
    }
}

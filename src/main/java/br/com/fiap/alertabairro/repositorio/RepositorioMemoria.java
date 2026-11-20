package br.com.fiap.alertabairro.repositorio;

import br.com.fiap.alertabairro.dominio.EntidadeBase;
import br.com.fiap.alertabairro.dominio.Validacao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Repositório em memória, usado no protótipo no lugar do banco de dados.
 * <p>
 * Gera ids sequenciais (1, 2, 3...), como uma sequence do Oracle, e mantém a
 * ordem de inserção.
 *
 * @param <T> tipo de entidade armazenada
 */
public class RepositorioMemoria<T extends EntidadeBase> implements Repositorio<T> {

    private final Map<Long, T> dados = new LinkedHashMap<>();
    private final String nomeEntidade;
    private long ultimoId;

    /**
     * Cria um repositório vazio.
     *
     * @param nomeEntidade nome usado nas mensagens de erro (ex.: "Ocorrência")
     */
    public RepositorioMemoria(String nomeEntidade) {
        this.nomeEntidade = Validacao.texto(nomeEntidade, "Nome da entidade", 2, 60);
    }

    @Override
    public T salvar(T entidade) {
        Validacao.obrigatorio(entidade, nomeEntidade);
        if (entidade.getId() == null) {
            entidade.atribuirId(++ultimoId);
        } else {
            ultimoId = Math.max(ultimoId, entidade.getId());
        }
        dados.put(entidade.getId(), entidade);
        return entidade;
    }

    @Override
    public Optional<T> buscarPorId(long id) {
        return Optional.ofNullable(dados.get(id));
    }

    @Override
    public List<T> listarTodos() {
        return Collections.unmodifiableList(new ArrayList<>(dados.values()));
    }

    @Override
    public String getNomeEntidade() {
        return nomeEntidade;
    }
}

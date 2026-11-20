package br.com.fiap.alertabairro.dominio;

import java.util.Objects;

/**
 * Base de todas as entidades do domínio: guarda o identificador único,
 * que corresponde à chave primária da tabela no banco de dados.
 * <p>
 * <b>Abstração:</b> a classe é abstrata porque não existe "uma entidade" genérica
 * no sistema, apenas bairros, ocorrências, sensores etc. Ela define o que todas
 * têm em comum (o {@code id}) e obriga cada uma a se descrever por
 * {@link #resumo()}.
 * <p>
 * <b>Encapsulamento:</b> o {@code id} não tem setter. Ele só pode ser atribuído
 * uma vez, pelo repositório, no momento em que a entidade é salva.
 */
public abstract class EntidadeBase {

    private Long id;

    /** Construtor usado pelas subclasses; a entidade nasce sem id. */
    protected EntidadeBase() {
    }

    /**
     * Retorna o identificador.
     *
     * @return o id, ou {@code null} se a entidade ainda não foi salva
     */
    public Long getId() {
        return id;
    }

    /**
     * Atribui o identificador. Só pode ser chamado uma vez.
     *
     * @param novoId valor positivo gerado pelo repositório
     * @throws IllegalStateException    se o id já tinha sido atribuído
     * @throws IllegalArgumentException se o valor não for positivo
     */
    public void atribuirId(long novoId) {
        if (this.id != null) {
            throw new IllegalStateException("O id de " + getClass().getSimpleName() + " já foi atribuído.");
        }
        if (novoId <= 0) {
            throw new IllegalArgumentException("O id deve ser positivo.");
        }
        this.id = novoId;
    }

    /**
     * Descrição curta e legível da entidade, usada em listagens e mensagens.
     * Cada subclasse define a sua (polimorfismo por sobrescrita).
     *
     * @return texto de uma linha
     */
    public abstract String resumo();

    /**
     * Duas entidades são iguais quando são do mesmo tipo e têm o mesmo id.
     * Antes de salvas (id nulo), só são iguais a si mesmas.
     */
    @Override
    public boolean equals(Object outro) {
        if (this == outro) {
            return true;
        }
        if (outro == null || getClass() != outro.getClass() || id == null) {
            return false;
        }
        return id.equals(((EntidadeBase) outro).id);
    }

    @Override
    public int hashCode() {
        return id == null ? System.identityHashCode(this) : Objects.hash(getClass(), id);
    }

    @Override
    public String toString() {
        return resumo();
    }
}

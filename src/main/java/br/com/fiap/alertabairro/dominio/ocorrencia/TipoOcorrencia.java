package br.com.fiap.alertabairro.dominio.ocorrencia;

import br.com.fiap.alertabairro.dominio.EntidadeBase;
import br.com.fiap.alertabairro.dominio.Validacao;

/**
 * Categoria do problema urbano (tabela TIPO_OCORRENCIA), como Alagamento ou
 * Bueiro obstruído.
 * <p>
 * O peso de risco (1 a 3) entra no cálculo do índice de risco da ocorrência:
 * um alagamento de gravidade 4 pesa mais que lixo acumulado de gravidade 4.
 */
public class TipoOcorrencia extends EntidadeBase {

    /** Menor peso de risco aceito. */
    public static final int PESO_MINIMO = 1;
    /** Maior peso de risco aceito. */
    public static final int PESO_MAXIMO = 3;

    private final String nome;
    private final String descricao;
    private final int pesoRisco;

    /**
     * Cria uma categoria.
     *
     * @param nome      nome da categoria (até 60 caracteres)
     * @param descricao explicação para o morador, ou {@code null}
     * @param pesoRisco peso de 1 a 3
     */
    public TipoOcorrencia(String nome, String descricao, int pesoRisco) {
        this.nome = Validacao.texto(nome, "Nome do tipo", 3, 60);
        this.descricao = Validacao.textoOpcional(descricao, "Descrição do tipo", 200);
        this.pesoRisco = Validacao.intervalo(pesoRisco, "Peso de risco", PESO_MINIMO, PESO_MAXIMO);
    }

    /**
     * Retorna nome da categoria.
     *
     * @return nome da categoria
     */
    public String getNome() {
        return nome;
    }

    /**
     * Retorna explicação da categoria, ou {@code null}.
     *
     * @return explicação da categoria, ou {@code null}
     */
    public String getDescricao() {
        return descricao;
    }

    /**
     * Retorna peso de risco de 1 a 3.
     *
     * @return peso de risco de 1 a 3
     */
    public int getPesoRisco() {
        return pesoRisco;
    }

    @Override
    public String resumo() {
        return "Tipo #" + getId() + " " + nome + " (peso " + pesoRisco + ")";
    }
}

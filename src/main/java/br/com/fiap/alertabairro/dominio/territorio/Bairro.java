package br.com.fiap.alertabairro.dominio.territorio;

import br.com.fiap.alertabairro.dominio.EntidadeBase;
import br.com.fiap.alertabairro.dominio.Validacao;
import br.com.fiap.alertabairro.dominio.excecao.RegraNegocioException;
import br.com.fiap.alertabairro.util.Formato;

/**
 * Região da cidade monitorada pela plataforma (tabela BAIRRO).
 * <p>
 * Permite comparar ocorrências entre bairros e direcionar alertas a quem mora ali.
 */
public class Bairro extends EntidadeBase {

    private final String nome;
    private final String cidade;
    private final String uf;
    private Integer populacaoEstimada;

    /**
     * Cria um bairro.
     *
     * @param nome              nome do bairro (2 a 80 caracteres)
     * @param cidade            cidade a que pertence
     * @param uf                sigla do estado com 2 letras
     * @param populacaoEstimada população aproximada, ou {@code null} se desconhecida
     * @throws RegraNegocioException se algum dado for inválido
     */
    public Bairro(String nome, String cidade, String uf, Integer populacaoEstimada) {
        this.nome = Validacao.texto(nome, "Nome do bairro", 2, 80);
        this.cidade = Validacao.texto(cidade, "Cidade", 2, 80);
        String sigla = Validacao.texto(uf, "UF", 2, 2).toUpperCase();
        if (!sigla.matches("[A-Z]{2}")) {
            throw new RegraNegocioException("UF inválida: " + uf);
        }
        this.uf = sigla;
        atualizarPopulacao(populacaoEstimada);
    }

    /**
     * Atualiza a população estimada.
     *
     * @param populacao novo valor positivo, ou {@code null}
     * @throws RegraNegocioException se o valor não for positivo
     */
    public final void atualizarPopulacao(Integer populacao) {
        if (populacao != null && populacao <= 0) {
            throw new RegraNegocioException("A população estimada deve ser maior que zero.");
        }
        this.populacaoEstimada = populacao;
    }

    /**
     * Retorna nome do bairro.
     *
     * @return nome do bairro
     */
    public String getNome() {
        return nome;
    }

    /**
     * Retorna cidade.
     *
     * @return cidade
     */
    public String getCidade() {
        return cidade;
    }

    /**
     * Retorna sigla do estado.
     *
     * @return sigla do estado
     */
    public String getUf() {
        return uf;
    }

    /**
     * Retorna população estimada, ou {@code null}.
     *
     * @return população estimada, ou {@code null}
     */
    public Integer getPopulacaoEstimada() {
        return populacaoEstimada;
    }

    /**
     * Retorna nome com cidade e estado, ex.: "Jardim Beira-Rio (São Paulo/SP)".
     *
     * @return nome com cidade e estado, ex.: "Jardim Beira-Rio (São Paulo/SP)"
     */
    public String getNomeCompleto() {
        return nome + " (" + cidade + "/" + uf + ")";
    }

    @Override
    public String resumo() {
        String pop = populacaoEstimada == null ? ""
                : String.format(Formato.BRASIL, " - %,d hab. (estimativa)", populacaoEstimada);
        return "Bairro #" + getId() + " " + getNomeCompleto() + pop;
    }
}

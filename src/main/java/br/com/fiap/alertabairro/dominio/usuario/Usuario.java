package br.com.fiap.alertabairro.dominio.usuario;

import br.com.fiap.alertabairro.dominio.EntidadeBase;
import br.com.fiap.alertabairro.dominio.Validacao;
import br.com.fiap.alertabairro.dominio.excecao.RegraNegocioException;
import br.com.fiap.alertabairro.dominio.territorio.Bairro;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Pessoa cadastrada na plataforma (tabela USUARIO).
 * <p>
 * <b>Herança e abstração:</b> a classe é abstrata e tem três especializações,
 * {@link Morador}, {@link AgenteComunitario} e {@link Administrador}, que no banco
 * ficam na mesma tabela e se diferenciam pela coluna {@code tp_perfil}.
 * <p>
 * <b>Polimorfismo:</b> cada subclasse sobrescreve {@link #getPerfil()},
 * {@link #getPermissoes()} e {@link #descreverPapel()}. O restante do sistema só
 * pergunta {@link #temPermissao(Permissao)}, sem precisar saber qual é o perfil.
 * <p>
 * <b>Proteção de dados:</b> o telefone só sai da classe mascarado, e a senha não
 * faz parte deste protótipo (no banco ela fica apenas como hash).
 */
public abstract class Usuario extends EntidadeBase {

    private static final Pattern EMAIL = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");

    private final String nome;
    private final String email;
    private String telefone;
    private Bairro bairroResidencia;
    private final LocalDateTime dataCadastro;
    private boolean ativo = true;

    /**
     * Construtor usado pelas subclasses.
     *
     * @param nome             nome de exibição
     * @param email            e-mail de acesso, em formato válido
     * @param telefone         telefone para SMS, ou {@code null}
     * @param bairroResidencia bairro onde mora, ou {@code null} (é opcional por privacidade)
     * @param dataCadastro     data e hora do cadastro
     */
    protected Usuario(String nome, String email, String telefone, Bairro bairroResidencia,
                      LocalDateTime dataCadastro) {
        this.nome = Validacao.texto(nome, "Nome", 2, 100);
        String emailLimpo = Validacao.texto(email, "E-mail", 6, 120).toLowerCase();
        if (!EMAIL.matcher(emailLimpo).matches()) {
            throw new RegraNegocioException("E-mail inválido: " + email);
        }
        this.email = emailLimpo;
        atualizarTelefone(telefone);
        this.bairroResidencia = bairroResidencia;
        this.dataCadastro = Validacao.obrigatorio(dataCadastro, "Data de cadastro");
    }

    /**
     * Perfil do usuário, definido por cada subclasse.
     *
     * @return MORADOR, AGENTE ou ADMINISTRADOR
     */
    public abstract PerfilUsuario getPerfil();

    /**
     * Conjunto de ações que o perfil pode executar, definido por cada subclasse.
     *
     * @return permissões do perfil
     */
    protected abstract Set<Permissao> getPermissoes();

    /**
     * Explica em uma frase o que a pessoa faz na plataforma.
     *
     * @return descrição do papel
     */
    public abstract String descreverPapel();

    /**
     * Verifica se o usuário pode executar uma ação. Contas desativadas não podem nada.
     *
     * @param permissao ação desejada
     * @return {@code true} se a conta está ativa e o perfil tem a permissão
     */
    public boolean temPermissao(Permissao permissao) {
        return ativo && getPermissoes().contains(permissao);
    }

    /**
     * Verifica se o usuário mora no bairro informado.
     *
     * @param bairro bairro de referência
     * @return {@code true} se o bairro de residência é o informado
     */
    public boolean moraEm(Bairro bairro) {
        return bairroResidencia != null && bairroResidencia.equals(bairro);
    }

    /**
     * Atualiza o telefone, validando o formato (somente dígitos e símbolos comuns).
     *
     * @param novoTelefone telefone, ou {@code null} para remover
     */
    public final void atualizarTelefone(String novoTelefone) {
        String limpo = Validacao.textoOpcional(novoTelefone, "Telefone", 20);
        if (limpo != null && !limpo.matches("[0-9()+\\- ]{8,20}")) {
            throw new RegraNegocioException("Telefone inválido: " + novoTelefone);
        }
        this.telefone = limpo;
    }

    /**
     * Muda o bairro de residência.
     *
     * @param novoBairro novo bairro, ou {@code null} para não informar
     */
    public void mudarBairro(Bairro novoBairro) {
        this.bairroResidencia = novoBairro;
    }

    /** Desativa a conta: o usuário deixa de ter qualquer permissão. */
    public void desativar() {
        this.ativo = false;
    }

    /** Reativa a conta. */
    public void ativar() {
        this.ativo = true;
    }

    /**
     * Telefone com os dígitos do meio escondidos, para exibição.
     *
     * @return por exemplo "(11) 9****-0001", ou "sem telefone"
     */
    public String getTelefoneMascarado() {
        if (telefone == null) {
            return "sem telefone";
        }
        String digitos = telefone.replaceAll("\\D", "");
        if (digitos.length() < 8) {
            return "****";
        }
        String ddd = digitos.length() >= 10 ? "(" + digitos.substring(0, 2) + ") " : "";
        String fim = digitos.substring(digitos.length() - 4);
        String inicio = digitos.length() >= 10 ? digitos.substring(2, 3) : digitos.substring(0, 1);
        return ddd + inicio + "****-" + fim;
    }

    /**
     * Retorna {@code true} se o usuário informou telefone.
     *
     * @return {@code true} se o usuário informou telefone
     */
    public boolean temTelefone() {
        return telefone != null;
    }

    /**
     * Retorna nome de exibição.
     *
     * @return nome de exibição
     */
    public String getNome() {
        return nome;
    }

    /**
     * Retorna e-mail de acesso.
     *
     * @return e-mail de acesso
     */
    public String getEmail() {
        return email;
    }

    /**
     * Retorna bairro de residência, ou {@code null}.
     *
     * @return bairro de residência, ou {@code null}
     */
    public Bairro getBairroResidencia() {
        return bairroResidencia;
    }

    /**
     * Retorna data de cadastro.
     *
     * @return data de cadastro
     */
    public LocalDateTime getDataCadastro() {
        return dataCadastro;
    }

    /**
     * Retorna {@code true} se a conta está ativa.
     *
     * @return {@code true} se a conta está ativa
     */
    public boolean isAtivo() {
        return ativo;
    }

    @Override
    public String resumo() {
        String bairro = bairroResidencia == null ? "bairro não informado" : bairroResidencia.getNome();
        return nome + " (" + getPerfil().getDescricao() + ") - " + bairro;
    }
}

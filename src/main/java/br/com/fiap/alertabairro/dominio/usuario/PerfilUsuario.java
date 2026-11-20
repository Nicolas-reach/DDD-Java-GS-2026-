package br.com.fiap.alertabairro.dominio.usuario;

/**
 * Papel do usuário na plataforma. Os nomes das constantes são os mesmos valores
 * aceitos na coluna {@code tp_perfil} da tabela USUARIO.
 */
public enum PerfilUsuario {

    /** Pessoa que mora na região, registra e acompanha ocorrências. */
    MORADOR("Morador"),
    /** Integrante da equipe comunitária de monitoramento. */
    AGENTE("Agente comunitário"),
    /** Responsável pela plataforma na associação de bairro. */
    ADMINISTRADOR("Administrador");

    private final String descricao;

    PerfilUsuario(String descricao) {
        this.descricao = descricao;
    }

    /**
     * Retorna nome amigável do perfil.
     *
     * @return nome amigável do perfil
     */
    public String getDescricao() {
        return descricao;
    }
}

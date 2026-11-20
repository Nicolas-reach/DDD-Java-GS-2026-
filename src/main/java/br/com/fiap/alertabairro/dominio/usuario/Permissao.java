package br.com.fiap.alertabairro.dominio.usuario;

/**
 * Ações controladas da plataforma. Cada perfil de usuário tem o seu conjunto
 * de permissões (ver {@link Usuario#getPermissoes()}).
 */
public enum Permissao {

    /** Registrar uma nova ocorrência. */
    REGISTRAR_OCORRENCIA("Registrar ocorrência"),
    /** Mover uma ocorrência entre os status de acompanhamento. */
    ATUALIZAR_STATUS("Atualizar status"),
    /** Descartar uma ocorrência improcedente ou de teste. */
    DESCARTAR_OCORRENCIA("Descartar ocorrência"),
    /** Encerrar um alerta comunitário que já não se aplica. */
    ENCERRAR_ALERTA("Encerrar alerta");

    private final String descricao;

    Permissao(String descricao) {
        this.descricao = descricao;
    }

    /**
     * Retorna nome amigável da permissão.
     *
     * @return nome amigável da permissão
     */
    public String getDescricao() {
        return descricao;
    }
}

package br.com.fiap.alertabairro.dominio.usuario;

import br.com.fiap.alertabairro.dominio.territorio.Bairro;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * Responsável pela plataforma na associação de bairro: tem todas as permissões,
 * inclusive descartar ocorrências improcedentes.
 */
public class Administrador extends Usuario {

    private static final Set<Permissao> PERMISSOES =
            Collections.unmodifiableSet(EnumSet.allOf(Permissao.class));

    /**
     * Cria um administrador.
     *
     * @param nome         nome de exibição
     * @param email        e-mail de acesso
     * @param telefone     telefone para SMS, ou {@code null}
     * @param bairro       bairro onde mora, ou {@code null}
     * @param dataCadastro data do cadastro
     */
    public Administrador(String nome, String email, String telefone, Bairro bairro,
                         LocalDateTime dataCadastro) {
        super(nome, email, telefone, bairro, dataCadastro);
    }

    @Override
    public PerfilUsuario getPerfil() {
        return PerfilUsuario.ADMINISTRADOR;
    }

    @Override
    protected Set<Permissao> getPermissoes() {
        return PERMISSOES;
    }

    @Override
    public String descreverPapel() {
        return "Gerencia a plataforma, decide o andamento e descarta registros improcedentes.";
    }
}

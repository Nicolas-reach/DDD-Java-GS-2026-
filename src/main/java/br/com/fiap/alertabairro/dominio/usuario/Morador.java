package br.com.fiap.alertabairro.dominio.usuario;

import br.com.fiap.alertabairro.dominio.territorio.Bairro;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * Morador da região: registra ocorrências e acompanha os registros de todos,
 * mas não altera status nem encerra alertas.
 */
public class Morador extends Usuario {

    private static final Set<Permissao> PERMISSOES =
            Collections.unmodifiableSet(EnumSet.of(Permissao.REGISTRAR_OCORRENCIA));

    /**
     * Cria um morador.
     *
     * @param nome         nome de exibição
     * @param email        e-mail de acesso
     * @param telefone     telefone para SMS, ou {@code null}
     * @param bairro       bairro onde mora, ou {@code null}
     * @param dataCadastro data do cadastro
     */
    public Morador(String nome, String email, String telefone, Bairro bairro, LocalDateTime dataCadastro) {
        super(nome, email, telefone, bairro, dataCadastro);
    }

    @Override
    public PerfilUsuario getPerfil() {
        return PerfilUsuario.MORADOR;
    }

    @Override
    protected Set<Permissao> getPermissoes() {
        return PERMISSOES;
    }

    @Override
    public String descreverPapel() {
        return "Registra problemas do bairro e acompanha o andamento pelo protocolo.";
    }
}

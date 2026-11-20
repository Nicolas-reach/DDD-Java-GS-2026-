package br.com.fiap.alertabairro.dominio.usuario;

import br.com.fiap.alertabairro.dominio.territorio.Bairro;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * Integrante da equipe comunitária de monitoramento: confere ocorrências no local,
 * atualiza o status e encerra alertas. Não pode descartar ocorrências, decisão
 * reservada ao administrador.
 */
public class AgenteComunitario extends Usuario {

    private static final Set<Permissao> PERMISSOES = Collections.unmodifiableSet(EnumSet.of(
            Permissao.REGISTRAR_OCORRENCIA,
            Permissao.ATUALIZAR_STATUS,
            Permissao.ENCERRAR_ALERTA));

    /**
     * Cria um agente comunitário.
     *
     * @param nome         nome de exibição
     * @param email        e-mail de acesso
     * @param telefone     telefone para SMS, ou {@code null}
     * @param bairro       bairro onde mora, ou {@code null}
     * @param dataCadastro data do cadastro
     */
    public AgenteComunitario(String nome, String email, String telefone, Bairro bairro,
                             LocalDateTime dataCadastro) {
        super(nome, email, telefone, bairro, dataCadastro);
    }

    @Override
    public PerfilUsuario getPerfil() {
        return PerfilUsuario.AGENTE;
    }

    @Override
    protected Set<Permissao> getPermissoes() {
        return PERMISSOES;
    }

    @Override
    public String descreverPapel() {
        return "Confere ocorrências no local, atualiza o andamento e encerra alertas.";
    }
}

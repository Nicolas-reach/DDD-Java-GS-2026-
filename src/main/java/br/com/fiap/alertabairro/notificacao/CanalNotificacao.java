package br.com.fiap.alertabairro.notificacao;

import br.com.fiap.alertabairro.dominio.monitoramento.Alerta;
import br.com.fiap.alertabairro.dominio.usuario.Usuario;

import java.util.List;

/**
 * Canal por onde um alerta chega às pessoas.
 * <p>
 * <b>Polimorfismo por interface:</b> o serviço de alertas recebe uma lista de
 * canais e chama {@link #enviar} em cada um, sem saber se é o painel, um SMS ou,
 * no futuro, WhatsApp ou e-mail. Para incluir um canal novo, basta criar outra
 * classe que implemente esta interface.
 */
public interface CanalNotificacao {

    /**
     * Nome do canal, para exibição.
     *
     * @return por exemplo "Painel comunitário"
     */
    String getNome();

    /**
     * Envia o alerta.
     *
     * @param alerta        alerta a divulgar
     * @param destinatarios moradores do bairro do alerta
     * @return quantas pessoas ou telas foram avisadas
     */
    int enviar(Alerta alerta, List<Usuario> destinatarios);
}

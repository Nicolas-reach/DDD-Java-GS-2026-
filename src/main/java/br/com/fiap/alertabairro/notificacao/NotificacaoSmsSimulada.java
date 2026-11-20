package br.com.fiap.alertabairro.notificacao;

import br.com.fiap.alertabairro.dominio.monitoramento.Alerta;
import br.com.fiap.alertabairro.dominio.usuario.Usuario;

import java.io.PrintStream;
import java.util.List;

/**
 * Simula o envio de SMS aos moradores do bairro que informaram telefone.
 * Nenhuma mensagem é enviada de verdade, e o número aparece mascarado.
 */
public class NotificacaoSmsSimulada implements CanalNotificacao {

    private final PrintStream saida;

    /**
     * Cria o canal.
     *
     * @param saida onde registrar os envios simulados
     */
    public NotificacaoSmsSimulada(PrintStream saida) {
        this.saida = saida;
    }

    @Override
    public String getNome() {
        return "SMS (simulado)";
    }

    @Override
    public int enviar(Alerta alerta, List<Usuario> destinatarios) {
        List<Usuario> comTelefone = destinatarios.stream().filter(Usuario::temTelefone).toList();
        String nomes = String.join(", ", comTelefone.stream()
                .map(u -> u.getNome() + " " + u.getTelefoneMascarado()).toList());
        saida.printf("      [SMS simulado] alerta #%d enviado para %d morador(es): %s%n",
                alerta.getId(), comTelefone.size(), comTelefone.isEmpty() ? "ninguém com telefone" : nomes);
        return comTelefone.size();
    }
}

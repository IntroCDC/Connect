package br.com.introcdc.connect.client.commands.message;
/*
 * Written by IntroCDC, Bruno Coêlho at 15/01/2025 - 17:42
 */

import br.com.introcdc.connect.client.command.ClientCommand;
import br.com.introcdc.connect.client.components.ChatComponents;

public class ClientCommandChat extends ClientCommand {

    public ClientCommandChat() {
        super("chat");
    }

    @Override
    public void execute(String command, String input) throws Exception {
        if (input.equalsIgnoreCase(">")) {
            ChatComponents.showChat(null);
        } else if (input.equalsIgnoreCase(">>")) {
            ChatComponents.CHAT_FRAME.setVisible(false);
        } else if (input.equalsIgnoreCase("clear")) {
            ChatComponents.CHAT_MESSAGES.clear();
            ChatComponents.updateTextArea();
        } else {
            ChatComponents.showChat("Servidor: " + input);
        }
    }

}

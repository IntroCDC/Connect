package br.com.introcdc.connect.server.commands.control;
/*
 * Written by IntroCDC, Bruno Coêlho at 15/01/2025 - 18:31
 */

import br.com.introcdc.connect.server.ConnectServerGUI;
import br.com.introcdc.connect.server.command.ServerCommand;
import br.com.introcdc.connect.server.components.ServerControlComponents;

public class ServerCommandKeyboardControl extends ServerCommand {

    public ServerCommandKeyboardControl() {
        super("keyboard");
    }

    @Override
    public void execute(String command, String input) throws Exception {
        if (ServerControlComponents.KEYBOARD) {
            ServerControlComponents.KEYBOARD = false;
            msg("Enviar teclas do teclado desativado!");
        } else {
            ServerControlComponents.KEYBOARD = true;
            msg("Enviar teclas do teclado ativado!");
        }
        ConnectServerGUI.toggleColor(ServerControlComponents.KEYBOARD_BUTTON, ServerControlComponents.KEYBOARD);
        ConnectServerGUI.toggleColor(ConnectServerGUI.KEYBOARD, ServerControlComponents.KEYBOARD);
    }

}

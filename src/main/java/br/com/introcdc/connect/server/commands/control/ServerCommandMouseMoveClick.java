package br.com.introcdc.connect.server.commands.control;
/*
 * Written by IntroCDC, Bruno Coêlho at 15/01/2025 - 18:33
 */

import br.com.introcdc.connect.server.ConnectServerGUI;
import br.com.introcdc.connect.server.command.ServerCommand;
import br.com.introcdc.connect.server.components.ServerControlComponents;

public class ServerCommandMouseMoveClick extends ServerCommand {

    public ServerCommandMouseMoveClick() {
        super("mousemoveclick");
    }

    @Override
    public void execute(String command, String input) throws Exception {
        if (ServerControlComponents.MOUSE_MOVE_CLICK) {
            ServerControlComponents.MOUSE_MOVE_CLICK = false;
            msg("Enviar movimento automático do mouse ao clicar desativado!");
        } else {
            ServerControlComponents.MOUSE_MOVE_CLICK = true;
            msg("Enviar movimento automático do mouse ao clicar ativado!");
        }
        ConnectServerGUI.toggleColor(ServerControlComponents.MOUSE_MOVE_CLICK_BUTTON, ServerControlComponents.MOUSE_MOVE_CLICK);
        ConnectServerGUI.toggleColor(ConnectServerGUI.MOUSE_MOVE_CLICK, ServerControlComponents.MOUSE_MOVE_CLICK);
    }

}

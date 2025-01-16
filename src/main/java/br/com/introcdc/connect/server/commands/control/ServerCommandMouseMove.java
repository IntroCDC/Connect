package br.com.introcdc.connect.server.commands.control;
/*
 * Written by IntroCDC, Bruno Coêlho at 15/01/2025 - 18:32
 */

import br.com.introcdc.connect.server.gui.ServerGUI;
import br.com.introcdc.connect.server.command.ServerCommand;
import br.com.introcdc.connect.server.components.ServerControlComponents;

public class ServerCommandMouseMove extends ServerCommand {

    public ServerCommandMouseMove() {
        super("mousemove");
    }

    @Override
    public void execute(String command, String input) throws Exception {
        if (ServerControlComponents.MOUSE_MOVE) {
            ServerControlComponents.MOUSE_MOVE = false;
            msg("Enviar movimento do mouse desativado!");
        } else {
            ServerControlComponents.MOUSE_MOVE = true;
            msg("Enviar movimento do mouse ativado!");
        }
        ServerGUI.toggleColor(ServerControlComponents.MOUSE_MOVE_BUTTON, ServerControlComponents.MOUSE_MOVE);
        ServerGUI.toggleColor(ServerGUI.MOUSE_MOVE, ServerControlComponents.MOUSE_MOVE);
    }

}

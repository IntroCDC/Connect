package br.com.introcdc.connect.server.commands.control;
/*
 * Written by IntroCDC, Bruno Coêlho at 15/01/2025 - 18:33
 */

import br.com.introcdc.connect.server.gui.ServerGUI;
import br.com.introcdc.connect.server.command.ServerCommand;
import br.com.introcdc.connect.server.components.ServerControlComponents;

public class ServerCommandMouse extends ServerCommand {

    public ServerCommandMouse() {
        super("mouse");
    }

    @Override
    public void execute(String command, String input) throws Exception {
        if (ServerControlComponents.MOUSE) {
            ServerControlComponents.MOUSE = false;
            msg("Enviar cliques do mouse desativado!");
        } else {
            ServerControlComponents.MOUSE = true;
            msg("Enviar cliques do mouse ativado!");
        }
        ServerGUI.toggleColor(ServerControlComponents.MOUSE_BUTTON, ServerControlComponents.MOUSE);
        ServerGUI.toggleColor(ServerGUI.MOUSE, ServerControlComponents.MOUSE);
    }

}

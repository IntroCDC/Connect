package br.com.introcdc.connect.server.commands.control;
/*
 * Written by IntroCDC, Bruno Coêlho at 15/01/2025 - 18:31
 */

import br.com.introcdc.connect.server.gui.ServerGUI;
import br.com.introcdc.connect.server.command.ServerCommand;
import br.com.introcdc.connect.server.components.ServerControlComponents;

public class ServerCommandServerControl extends ServerCommand {

    public ServerCommandServerControl() {
        super("control");
    }

    @Override
    public void execute(String command, String input) throws Exception {
        if (ServerControlComponents.CONTROL) {
            ServerControlComponents.CONTROL = false;
            msg("Modo control remoto desativado!");
        } else {
            ServerControlComponents.CONTROL = true;
            msg("Modo control remoto ativado!");
        }
        ServerGUI.toggleColor(ServerControlComponents.CONTROL_BUTTON, ServerControlComponents.CONTROL);
        ServerGUI.toggleColor(ServerGUI.controlButton, ServerControlComponents.CONTROL);
        ServerGUI.toggleColor(ServerGUI.CONTROL, ServerControlComponents.CONTROL);
    }

}

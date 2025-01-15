package br.com.introcdc.connect.server.commands.control;
/*
 * Written by IntroCDC, Bruno Coêlho at 15/01/2025 - 18:31
 */

import br.com.introcdc.connect.server.ConnectServerGUI;
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
        ConnectServerGUI.toggleColor(ServerControlComponents.CONTROL_BUTTON, ServerControlComponents.CONTROL);
        ConnectServerGUI.toggleColor(ConnectServerGUI.controlButton, ServerControlComponents.CONTROL);
        ConnectServerGUI.toggleColor(ConnectServerGUI.CONTROL, ServerControlComponents.CONTROL);
    }

}

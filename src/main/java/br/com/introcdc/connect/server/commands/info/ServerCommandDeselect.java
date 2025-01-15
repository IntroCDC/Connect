package br.com.introcdc.connect.server.commands.info;
/*
 * Written by IntroCDC, Bruno Coêlho at 15/01/2025 - 18:30
 */

import br.com.introcdc.connect.server.ConnectServer;
import br.com.introcdc.connect.server.command.ServerCommand;

public class ServerCommandDeselect extends ServerCommand {

    public ServerCommandDeselect() {
        super("desel");
    }

    @Override
    public void execute(String command, String input) throws Exception {
        ConnectServer.SELECTED_CLIENT = 0;
        msg("Cliente selecionado resetado!");
    }

}

package br.com.introcdc.connect.server.commands.info;
/*
 * Written by IntroCDC, Bruno Coêlho at 15/01/2025 - 18:42
 */

import br.com.introcdc.connect.server.ConnectServer;
import br.com.introcdc.connect.server.command.ServerCommand;

public class ServerCommandDuplicate extends ServerCommand {

    public ServerCommandDuplicate() {
        super("duplicate");
    }

    @Override
    public void execute(String command, String input) throws Exception {
        if (ConnectServer.DISCONNECT_DUPLICATE) {
            ConnectServer.DISCONNECT_DUPLICATE = false;
            msg("Modo desconectar duplicata desativado!");
        } else {
            ConnectServer.DISCONNECT_DUPLICATE = true;
            msg("Modo desconectar duplicata ativado!");
        }
    }

}

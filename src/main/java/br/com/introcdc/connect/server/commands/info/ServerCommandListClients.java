package br.com.introcdc.connect.server.commands.info;
/*
 * Written by IntroCDC, Bruno Coêlho at 15/01/2025 - 18:29
 */

import br.com.introcdc.connect.server.ConnectServer;
import br.com.introcdc.connect.server.command.ServerCommand;
import br.com.introcdc.connect.server.connection.ClientHandler;

import java.util.Collection;

public class ServerCommandListClients extends ServerCommand {

    public ServerCommandListClients() {
        super("list");
    }

    @Override
    public void execute(String command, String input) throws Exception {
        Collection<ClientHandler> clientHandlers = ConnectServer.CLIENTS.values();
        if (clientHandlers.isEmpty()) {
            msg("Nenhum cliente conectado no momento!");
            return;
        }
        for (ClientHandler clientHandler : clientHandlers) {
            msg(clientHandler.getClientInfo());
        }
    }

}

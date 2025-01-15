package br.com.introcdc.connect.client.commands.info;
/*
 * Written by IntroCDC, Bruno Coêlho at 15/01/2025 - 16:11
 */

import br.com.introcdc.connect.client.ConnectClient;
import br.com.introcdc.connect.client.command.ClientCommand;

public class ClientCommandDebug extends ClientCommand {

    public ClientCommandDebug() {
        super("debug");
    }

    @Override
    public void execute(String command, String input) throws Exception {
        if (ConnectClient.DEBUG) {
            ConnectClient.DEBUG = false;
            msg("Modo debug desativado!");
        } else {
            ConnectClient.DEBUG = true;
            msg("Modo debug ativado!");
        }
    }

}

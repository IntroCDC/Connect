package br.com.introcdc.connect.client.commands.info;
/*
 * Written by IntroCDC, Bruno Coêlho at 15/01/2025 - 17:43
 */

import br.com.introcdc.connect.client.command.ClientCommand;

public class ClientCommandClose extends ClientCommand {

    public ClientCommandClose() {
        super("close");
    }

    @Override
    public void execute(String command, String input) throws Exception {
        msg("Programa fechando!");
        System.exit(0);
    }

}

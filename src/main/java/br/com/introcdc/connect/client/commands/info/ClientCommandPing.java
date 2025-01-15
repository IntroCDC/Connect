package br.com.introcdc.connect.client.commands.info;
/*
 * Written by IntroCDC, Bruno Coêlho at 15/01/2025 - 16:06
 */

import br.com.introcdc.connect.client.command.ClientCommand;

public class ClientCommandPing extends ClientCommand {

    public ClientCommandPing() {
        super("ping");
    }

    @Override
    public void execute(String command, String input) throws Exception {
        msg("ping");
    }

}

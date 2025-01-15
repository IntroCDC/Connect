package br.com.introcdc.connect.client.commands.info;
/*
 * Written by IntroCDC, Bruno Coêlho at 15/01/2025 - 18:36
 */

import br.com.introcdc.connect.client.command.ClientCommand;

public class ClientCommandIgnore extends ClientCommand {

    public ClientCommandIgnore() {
        super("ignore");
    }

    @Override
    public void execute(String command, String input) throws Exception {
    }

}

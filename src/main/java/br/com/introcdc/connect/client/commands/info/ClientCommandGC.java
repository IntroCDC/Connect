package br.com.introcdc.connect.client.commands.info;
/*
 * Written by IntroCDC, Bruno Coêlho at 15/01/2025 - 16:11
 */

import br.com.introcdc.connect.client.command.ClientCommand;

public class ClientCommandGC extends ClientCommand {

    public ClientCommandGC() {
        super("gc");
    }

    @Override
    public void execute(String command, String input) throws Exception {
        try {
            msg("Liberando espaço na memória ram...");
            System.gc();
            msg("Espaço na memória ram liberado!");
        } catch (Exception exception) {
            msg("Ocorreu um erro ao liberar espaço na memória ram! (" + exception.getMessage() + ")");
            exception(exception);
        }
    }

}

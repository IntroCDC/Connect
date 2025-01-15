package br.com.introcdc.connect.client.commands.info;
/*
 * Written by IntroCDC, Bruno Coêlho at 15/01/2025 - 17:44
 */

import br.com.introcdc.connect.client.command.ClientCommand;
import br.com.introcdc.connect.client.components.InstallComponents;

public class ClientCommandUpdate extends ClientCommand {

    public ClientCommandUpdate() {
        super("update");
    }

    @Override
    public void execute(String command, String input) throws Exception {
        msg("Verificando atualização...");
        InstallComponents.verifyUpdate();
    }

}

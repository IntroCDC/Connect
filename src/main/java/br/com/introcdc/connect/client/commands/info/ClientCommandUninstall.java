package br.com.introcdc.connect.client.commands.info;
/*
 * Written by IntroCDC, Bruno Coêlho at 15/01/2025 - 17:43
 */

import br.com.introcdc.connect.client.command.ClientCommand;
import br.com.introcdc.connect.client.components.InstallComponents;

public class ClientCommandUninstall extends ClientCommand {

    public ClientCommandUninstall() {
        super("uninstall");
    }

    @Override
    public void execute(String command, String input) throws Exception {
        msg("Desinstalando e fechando!");
        InstallComponents.uninstall();
        System.exit(0);
    }

}

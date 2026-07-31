package br.com.introcdc.connect.client.commands.image;
/*
 * Written by IntroCDC, Bruno Coêlho at 15/01/2025 - 17:34
 */

import br.com.introcdc.connect.client.command.ClientCommand;
import br.com.introcdc.connect.client.components.ClientImageComponents;

public class ClientCommandLiveStopper extends ClientCommand {

    public ClientCommandLiveStopper() {
        super("livestopper");
    }

    @Override
    public void execute(String command, String input) throws Exception {
        if (ClientImageComponents.LIVE_STOPPER) {
            ClientImageComponents.LIVE_STOPPER = false;
            msg("Parador de atualizador de live automático desativado!");
        } else {
            ClientImageComponents.LIVE_STOPPER = true;
            msg("Parador de atualizador de live automático ativado!");
        }
    }

}

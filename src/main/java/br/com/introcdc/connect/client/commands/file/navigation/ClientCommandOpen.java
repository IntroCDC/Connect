package br.com.introcdc.connect.client.commands.file.navigation;
/*
 * Written by IntroCDC, Bruno Coêlho at 24/01/2025 - 08:48
 */

import br.com.introcdc.connect.client.command.ClientCommand;
import br.com.introcdc.connect.client.components.FileComponents;

import java.awt.*;
import java.io.File;

public class ClientCommandOpen extends ClientCommand {

    public ClientCommandOpen() {
        super("open");
    }

    @Override
    public void execute(String command, String input) throws Exception {
        File file = FileComponents.file(input);
        if (!file.exists()) {
            msg("Arquivo ou pasta não encontrado!");
            return;
        }

        try {
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();

                if (desktop.isSupported(Desktop.Action.OPEN)) {
                    desktop.open(file);
                } else {
                    msg("Ação de abrir arquivos não é suportada neste sistema.");
                }
            } else {
                msg("Desktop não é suportado no ambiente atual.");
            }
        } catch (Exception exception) {
            msg("Ocorreu um erro ao abrir o arquivo ou pasta " + file.getName() + "! (" + exception.getMessage() + ")");
            exception(exception);
        }
    }

}

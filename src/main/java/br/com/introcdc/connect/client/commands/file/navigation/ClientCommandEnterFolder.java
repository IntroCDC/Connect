package br.com.introcdc.connect.client.commands.file.navigation;
/*
 * Written by IntroCDC, Bruno Coêlho at 15/01/2025 - 16:34
 */

import br.com.introcdc.connect.client.command.ClientCommand;
import br.com.introcdc.connect.client.components.FileComponents;

import java.io.File;

public class ClientCommandEnterFolder extends ClientCommand {

    public ClientCommandEnterFolder() {
        super("cd");
    }

    @Override
    public void execute(String command, String input) throws Exception {
        input = input
                .replace("user.dir", System.getProperty("user.dir"))
                .replace("user.home", System.getProperty("user.home"))
                .replace("user.name", System.getProperty("user.name"));
        if (input.isEmpty()) {
            msg("Digite um arquivo ou pasta!");
            return;
        }
        if (input.equalsIgnoreCase("..")) {
            File access = new File(FileComponents.FOLDER).getParentFile();
            if (access == null) {
                msg("Não foi possível acessar uma pasta anterior!");
                return;
            }
            FileComponents.FOLDER = access.getAbsolutePath();
            msg("Acessado: " + access.getAbsolutePath());
            return;
        }
        File file = FileComponents.file(input);
        if (!file.exists()) {
            msg("Pasta não encontrada!");
            return;
        }
        if (file.isDirectory() && file.listFiles() != null) {
            FileComponents.FOLDER = file.getAbsolutePath();
            msg("Acessado: " + file.getAbsolutePath());
        } else {
            msg("Não é possível acessar esta pasta!");
        }
    }

}

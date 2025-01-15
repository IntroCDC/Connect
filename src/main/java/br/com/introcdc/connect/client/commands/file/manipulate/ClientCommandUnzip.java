package br.com.introcdc.connect.client.commands.file.manipulate;
/*
 * Written by IntroCDC, Bruno Coêlho at 15/01/2025 - 16:49
 */

import br.com.introcdc.connect.client.command.ClientCommand;
import br.com.introcdc.connect.client.components.FileComponents;

import java.io.File;

public class ClientCommandUnzip extends ClientCommand {

    public ClientCommandUnzip() {
        super("unzip");
    }

    @Override
    public void execute(String command, String input) throws Exception {
        if (input.isEmpty()) {
            msg("Digite um arquivo!");
            return;
        }
        File file = FileComponents.file(input);
        if (!file.exists() || !file.isFile() || !file.getName().toLowerCase().endsWith(".zip")) {
            msg("Arquivo não encontrado!");
            return;
        }
        msg("Extraindo arquivo...");
        File folder = new File(file.getName().replace(".zip", ""));
        if (!folder.exists()) {
            folder.mkdirs();
        }
        FileComponents.extractZip(file, folder);
        msg("Arquivo extraído!");
    }

}

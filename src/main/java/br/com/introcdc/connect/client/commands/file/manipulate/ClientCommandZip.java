package br.com.introcdc.connect.client.commands.file.manipulate;
/*
 * Written by IntroCDC, Bruno Coêlho at 15/01/2025 - 16:48
 */

import br.com.introcdc.connect.client.command.ClientCommand;
import br.com.introcdc.connect.client.components.FileComponents;

import java.io.File;

public class ClientCommandZip extends ClientCommand {

    public ClientCommandZip() {
        super("zip");
    }

    @Override
    public void execute(String command, String input) throws Exception {
        if (input.isEmpty()) {
            msg("Digite um arquivo!");
            return;
        }
        File file = FileComponents.file(input);
        if (!file.exists()) {
            msg("Arquivo não encontrado!");
            return;
        }
        msg("Zipando arquivo...");
        if (file.isDirectory()) {
            FileComponents.createZip(file, new File(FileComponents.FOLDER, file.getName() + ".zip"), FileComponents.FOLDER.replace("\\", "/") + "/");
        } else {
            FileComponents.createZipFile(file);
        }
        msg("Arquivo zipado!");
    }

}

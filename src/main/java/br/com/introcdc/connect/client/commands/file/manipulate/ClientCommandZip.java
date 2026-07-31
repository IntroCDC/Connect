package br.com.introcdc.connect.client.commands.file.manipulate;
/*
 * Written by IntroCDC, Bruno Coêlho at 15/01/2025 - 16:48
 */

import br.com.introcdc.connect.client.command.ClientCommand;
import br.com.introcdc.connect.client.components.ClientFileComponents;

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
        File file = ClientFileComponents.file(input);
        if (!file.exists()) {
            msg("Arquivo não encontrado!");
            return;
        }
        msg("Zipando arquivo...");
        if (file.isDirectory()) {
            ClientFileComponents.createZip(file, new File(ClientFileComponents.FOLDER, file.getName() + ".zip"), ClientFileComponents.FOLDER.replace("\\", "/") + "/");
        } else {
            ClientFileComponents.createZipFile(file);
        }
        msg("Arquivo zipado!");
    }

}

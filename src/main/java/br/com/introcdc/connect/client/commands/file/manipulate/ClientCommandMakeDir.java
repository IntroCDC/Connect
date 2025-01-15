package br.com.introcdc.connect.client.commands.file.manipulate;
/*
 * Written by IntroCDC, Bruno Coêlho at 15/01/2025 - 16:30
 */

import br.com.introcdc.connect.client.command.ClientCommand;
import br.com.introcdc.connect.client.components.FileComponents;

import java.io.File;

public class ClientCommandMakeDir extends ClientCommand {

    public ClientCommandMakeDir() {
        super("mkdir");
    }

    @Override
    public void execute(String command, String input) throws Exception {
        if (input.isEmpty()) {
            msg("Digite um nome para criar uma pasta!");
            return;
        }
        File folder = FileComponents.newFile(input);
        if (folder.exists() && folder.isDirectory()) {
            msg("Esta pasta já existe!");
            return;
        }
        if (folder.mkdir() || folder.mkdirs()) {
            msg("Pasta " + folder.getAbsolutePath() + " criada!");
        } else {
            msg("Não foi possível criar a pasta " + folder.getAbsolutePath() + "!");
        }
    }

}

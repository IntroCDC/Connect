package br.com.introcdc.connect.client.commands.file.manipulate;
/*
 * Written by IntroCDC, Bruno Coêlho at 15/01/2025 - 16:19
 */

import br.com.introcdc.connect.client.command.ClientCommand;
import br.com.introcdc.connect.client.components.FileComponents;

import java.io.File;

public class ClientCommandDel extends ClientCommand {

    public ClientCommandDel() {
        super("del");
    }

    @Override
    public void execute(String command, String input) throws Exception {
        if (input.isEmpty()) {
            msg("Digite um arquivo ou pasta!");
            return;
        }
        File file = FileComponents.file(input);
        if (!file.exists()) {
            msg("Arquivo não encontrado!");
            return;
        }
        FileComponents.deleteFile(file);
        msg("Arquivo " + file.getAbsolutePath() + " apagado!");
    }

}

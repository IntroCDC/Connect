package br.com.introcdc.connect.client.commands.file.manipulate;
/*
 * Written by IntroCDC, Bruno Coêlho at 15/01/2025 - 16:29
 */

import br.com.introcdc.connect.client.command.ClientCommand;
import br.com.introcdc.connect.client.components.FileComponents;

import java.io.File;

public class ClientCommandCopy extends ClientCommand {

    public ClientCommandCopy() {
        super("copy");
    }

    @Override
    public void execute(String command, String input) throws Exception {
        String separator = "-/-";
        if (input.isEmpty() || input.split(separator, 2).length != 2) {
            msg("Digite um arquivo ou pasta!");
            msg("Comando: copy (arquivo)" + separator + "(novo objetivo)");
            return;
        }
        if (input.split(separator, 2)[0].isEmpty()) {
            msg("Digite o nome do arquivo inicial!");
            return;
        }
        File file = FileComponents.file(input.split(separator, 2)[0]);
        if (!file.exists()) {
            msg("Arquivo não encontrado!");
            return;
        }
        if (input.split(separator, 2)[1].isEmpty()) {
            msg("Digite o nome do arquivo objetivo completo!");
            return;
        }
        File to = FileComponents.newFile(input.split(separator, 2)[1]);
        if (file.isFile() && to.isDirectory()) {
            to = new File(to, file.getName());
        }
        FileComponents.copy(file, to);
        msg("Arquivo " + file.getAbsolutePath() + " copiado para " + to.getAbsolutePath() + "!");
    }

}

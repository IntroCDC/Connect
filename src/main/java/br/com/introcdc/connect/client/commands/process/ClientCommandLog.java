package br.com.introcdc.connect.client.commands.process;
/*
 * Written by IntroCDC, Bruno Coêlho at 15/01/2025 - 17:37
 */

import br.com.introcdc.connect.client.command.ClientCommand;
import br.com.introcdc.connect.client.components.ClientProcessComponents;

public class ClientCommandLog extends ClientCommand {

    public ClientCommandLog() {
        super("log");
    }

    @Override
    public void execute(String command, String input) throws Exception {
        if (input.isEmpty()) {
            msg("Digite um id de programa!");
            return;
        }
        try {
            Integer id = Integer.valueOf(input);
            if (!ClientProcessComponents.PROCESS_LIST.containsKey(id)) {
                msg("Processo não encontrado!");
                return;
            }
            if (ClientProcessComponents.LOG_PROCESS.contains(id)) {
                msg("Agora você não está mais recebendo logs do processo #" + id);
                ClientProcessComponents.LOG_PROCESS.remove(id);
            } else {
                msg("Agora você está recebendo logs do processo #" + id);
                ClientProcessComponents.LOG_PROCESS.add(id);
            }
        } catch (Exception ignored) {
            msg("Digite um número válido!");
        }
    }

}

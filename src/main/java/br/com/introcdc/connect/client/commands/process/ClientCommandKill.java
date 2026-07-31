package br.com.introcdc.connect.client.commands.process;
/*
 * Written by IntroCDC, Bruno Coêlho at 15/01/2025 - 17:38
 */

import br.com.introcdc.connect.client.command.ClientCommand;
import br.com.introcdc.connect.client.components.ClientProcessComponents;

public class ClientCommandKill extends ClientCommand {

    public ClientCommandKill() {
        super("kill");
    }

    @Override
    public void execute(String command, String input) throws Exception {
        if (input.isEmpty()) {
            msg("Digite um id de processo!");
            return;
        }
        try {
            Integer id = Integer.valueOf(input);
            if (!ClientProcessComponents.PROCESS_LIST.containsKey(id)) {
                msg("Processo não encontrado!");
                return;
            }
            msg("Processo #" + id + " (" + ClientProcessComponents.PROCESS_LIST.remove(id) + ") finalizado!");
            ClientProcessComponents.PROCESS_MAP.remove(id).destroy();
            ClientProcessComponents.LOG_PROCESS.remove(id);
            ClientProcessComponents.WRITER_MAP.remove(id).close();
        } catch (Exception ignored) {
            msg("Digite um número válido!");
        }
    }

}

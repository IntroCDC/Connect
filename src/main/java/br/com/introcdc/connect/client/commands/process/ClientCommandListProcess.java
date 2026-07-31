package br.com.introcdc.connect.client.commands.process;
/*
 * Written by IntroCDC, Bruno Coêlho at 15/01/2025 - 17:39
 */

import br.com.introcdc.connect.client.command.ClientCommand;
import br.com.introcdc.connect.client.components.ClientProcessComponents;

public class ClientCommandListProcess extends ClientCommand {

    public ClientCommandListProcess() {
        super("listprocess");
    }

    @Override
    public void execute(String command, String input) throws Exception {
        if (ClientProcessComponents.PROCESS_MAP.isEmpty()) {
            msg("Não há nenhum processo executando pelo programa agora!");
            return;
        }
        for (Integer id : ClientProcessComponents.PROCESS_LIST.keySet()) {
            msg("#" + id + ": " + ClientProcessComponents.PROCESS_LIST.get(id));
        }
    }

}

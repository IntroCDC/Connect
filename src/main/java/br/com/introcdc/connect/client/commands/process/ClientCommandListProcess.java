package br.com.introcdc.connect.client.commands.process;
/*
 * Written by IntroCDC, Bruno Coêlho at 15/01/2025 - 17:39
 */

import br.com.introcdc.connect.client.command.ClientCommand;
import br.com.introcdc.connect.client.components.ProcessComponents;

public class ClientCommandListProcess extends ClientCommand {

    public ClientCommandListProcess() {
        super("listprocess");
    }

    @Override
    public void execute(String command, String input) throws Exception {
        if (ProcessComponents.PROCESS_MAP.isEmpty()) {
            msg("Não há nenhum processo executando pelo programa agora!");
            return;
        }
        for (Integer id : ProcessComponents.PROCESS_LIST.keySet()) {
            msg("#" + id + ": " + ProcessComponents.PROCESS_LIST.get(id));
        }
    }

}

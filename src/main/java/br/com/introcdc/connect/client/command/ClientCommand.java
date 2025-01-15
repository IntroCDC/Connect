package br.com.introcdc.connect.client.command;
/*
 * Written by IntroCDC, Bruno Coêlho at 15/01/2025 - 16:04
 */

import br.com.introcdc.connect.client.ConnectClient;

public abstract class ClientCommand {

    private final String[] commands;

    public ClientCommand(String... commands) {
        this.commands = commands;
    }

    public String[] getCommands() {
        return commands;
    }

    public abstract void execute(String command, String input) throws Exception;

    public void msg(String message) {
        ConnectClient.msg(message);
    }

    public void exception(Exception exception) {
        ConnectClient.exception(exception);
    }

    public static void handleCommand(String input) {
        String[] args = input.split(" ", 2);

        String command = args[0].toLowerCase();
        String inputString = args.length == 1 ? "" : args[1];

        if (ClientCommandEnum.getCommandMap().containsKey(command)) {
            try {
                ClientCommandEnum.getCommandMap().get(command).execute(command, inputString);
            } catch (Exception exception) {
                ConnectClient.msg("Ocorreu um erro ao executar o comando '" + input + "'! (" + exception.getMessage() + ")");
                ConnectClient.exception(exception);
            }
            return;
        }

        ConnectClient.msg("Comando desconhecido!");
    }

}

package br.com.introcdc.connect.server.command;
/*
 * Written by IntroCDC, Bruno Coêlho at 15/01/2025 - 18:27
 */

import br.com.introcdc.connect.server.ConnectServer;

public abstract class ServerCommand {

    private final String[] commands;

    public ServerCommand(String... commands) {
        this.commands = commands;
    }

    public String[] getCommands() {
        return commands;
    }

    public abstract void execute(String command, String input) throws Exception;

    public void msg(String message) {
        ConnectServer.msg(message);
    }

    public static boolean handleCommand(String input) {
        String[] args = input.split(" ", 2);

        String command = args[0].toLowerCase();
        String inputString = args.length == 1 ? "" : args[1];

        if (ServerCommandEnum.getCommandMap().containsKey(command)) {
            try {
                ServerCommandEnum.getCommandMap().get(command).execute(command, inputString);
            } catch (Exception exception) {
                ConnectServer.msg("Ocorreu um erro ao executar o comando '" + input + "'! (" + exception.getMessage() + ")");
            }
            return true;
        }
        return false;
    }

}

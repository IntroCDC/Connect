package br.com.introcdc.connect.server.command;
/*
 * Written by IntroCDC, Bruno Coêlho at 15/01/2025 - 18:52
 */

import br.com.introcdc.connect.server.ConnectServer;
import br.com.introcdc.connect.server.commands.control.*;
import br.com.introcdc.connect.server.commands.info.*;

import java.util.HashMap;
import java.util.Map;

public enum ServerCommandEnum {
    // Control
    FPS(ServerCommandFPS.class),
    KEYBOARD_CONTROL(ServerCommandKeyboardControl.class),
    MOUSE(ServerCommandMouse.class),
    MOUSE_MOVE(ServerCommandMouseMove.class),
    MOUVE_MOVE_CLICK(ServerCommandMouseMoveClick.class),
    SERVER_CONTROL(ServerCommandServerControl.class),

    // Info
    DESELECT(ServerCommandDeselect.class),
    DUPLICATE(ServerCommandDuplicate.class),
    HELP(ServerCommandHelp.class),
    LIST_CLIENTS(ServerCommandListClients.class),
    SELECT(ServerCommandSelect.class);

    private static Map<String, ServerCommand> COMMAND_MAP = new HashMap<>();

    public static Map<String, ServerCommand> getCommandMap() {
        return COMMAND_MAP;
    }

    private final Class<? extends ServerCommand> commandClass;

    ServerCommandEnum(Class<? extends ServerCommand> commandClass) {
        this.commandClass = commandClass;
    }

    public Class<? extends ServerCommand> getCommandClass() {
        return commandClass;
    }

    public static void registerCommands() {
        for (ServerCommandEnum clientCommandEnum : values()) {
            try {
                ServerCommand clientCommand = clientCommandEnum.getCommandClass().newInstance();
                for (String commandString : clientCommand.getCommands()) {
                    getCommandMap().put(commandString, clientCommand);
                }
            } catch (Exception exception) {
                ConnectServer.msg("Ocorreu um erro ao registrar o comando: " + clientCommandEnum.toString() + " (" + exception.getMessage() + ")");
            }
        }
    }

}

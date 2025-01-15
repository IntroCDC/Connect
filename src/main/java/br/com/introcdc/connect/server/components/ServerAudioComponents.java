package br.com.introcdc.connect.server.components;
/*
 * Written by IntroCDC, Bruno Coêlho at 15/01/2025 - 18:20
 */

import br.com.introcdc.connect.server.ConnectServer;
import br.com.introcdc.connect.server.ConnectServerGUI;
import br.com.introcdc.connect.server.connection.ClientHandler;
import javazoom.jl.player.Player;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import java.awt.*;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class ServerAudioComponents {

    public static Player CURRENT_PLAYER;
    public static boolean AUDIO_USER = false;
    public static boolean AUDIO_SERVER = false;

    public static void playText(String text) {
        if (text == null) {
            return;
        }
        try {
            if (CURRENT_PLAYER != null) {
                CURRENT_PLAYER.close();
            }

            String encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8.toString());
            String audioUrl = "http://api.kindome.com.br/voice/" + encodedText;

            new Thread(() -> {
                try (InputStream is = new URL(audioUrl).openStream()) {
                    CURRENT_PLAYER = new Player(is);
                    CURRENT_PLAYER.play();
                } catch (Exception exception) {
                    ConnectServer.msg("Ocorreu um erro ao executar a voz: " + text);
                }
            }).start();
        } catch (Exception exception) {
            ConnectServer.msg("Ocorreu um erro ao executar a voz: " + text);
        }
    }

    public static void generateBeep(int durationMs, int frequencyHz, boolean thread) {
        Runnable runnable = () -> {
            try {
                float sampleRate = 44100;
                byte[] buffer = new byte[1];
                AudioFormat format = new AudioFormat(sampleRate, 8, 1, true, true);

                SourceDataLine line = AudioSystem.getSourceDataLine(format);
                line.open(format);
                line.start();

                for (int i = 0; i < durationMs * (sampleRate / 1000); i++) {
                    double angle = i / (sampleRate / frequencyHz) * 2.0 * Math.PI;
                    buffer[0] = (byte) (Math.sin(angle) * 127);
                    line.write(buffer, 0, 1);
                }

                line.drain();
                line.close();
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        };
        if (thread) {
            new Thread(runnable).start();
        } else {
            runnable.run();
        }
    }

    public static void handleAudio(String input) {
        if (ConnectServer.SELECTED_CLIENT == -1) {
            ConnectServer.msg("Não é possível abrir o control de áudio para multiplos usuários!");
        } else {
            ClientHandler client = ConnectServer.CLIENTS.get(ConnectServer.SELECTED_CLIENT);
            if (input.equalsIgnoreCase("controls")) {
                ConnectServerGUI.getInstance().audioControlPanel();
            } else {
                if (input.equalsIgnoreCase("receive")) {
                    if (ConnectServerGUI.AUDIO_USER != null) {
                        if (ServerAudioComponents.AUDIO_USER) {
                            ServerAudioComponents.AUDIO_USER = false;
                            ConnectServerGUI.AUDIO_USER.setBackground(Color.RED);
                        } else {
                            ConnectServerGUI.AUDIO_USER.setBackground(Color.YELLOW);
                        }
                    }
                } else if (input.equalsIgnoreCase("send")) {
                    if (ConnectServerGUI.AUDIO_SERVER != null) {
                        if (ServerAudioComponents.AUDIO_SERVER) {
                            ServerAudioComponents.AUDIO_SERVER = false;
                            ConnectServerGUI.AUDIO_SERVER.setBackground(Color.RED);
                        } else {
                            ConnectServerGUI.AUDIO_SERVER.setBackground(Color.YELLOW);
                        }
                    }
                }
                client.send("audio " + input);
            }
        }
    }

}

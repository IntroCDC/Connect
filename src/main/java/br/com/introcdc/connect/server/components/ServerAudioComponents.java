package br.com.introcdc.connect.server.components;
/*
 * Written by IntroCDC, Bruno Coêlho at 15/01/2025 - 18:20
 */

import br.com.introcdc.connect.server.ConnectServer;
import br.com.introcdc.connect.server.connection.ClientHandler;
import br.com.introcdc.connect.server.gui.ServerGUI;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

public class ServerAudioComponents {

    public static boolean AUDIO_USER = false;
    public static boolean AUDIO_SERVER = false;

    public static void playText(String text) {
        if (text == null) {
            return;
        }
        try {
            String vbsScript = "Dim sapi\n"
                    + "Set sapi = CreateObject(\"SAPI.SpVoice\")\n"
                    + "sapi.Speak \"" + text + "\"";

            new Thread(() -> {
                try {
                    File file = new File("tts.vbs");
                    FileOutputStream fos = new FileOutputStream(file);
                    OutputStreamWriter osw = new OutputStreamWriter(fos, "windows-1252");
                    osw.write(vbsScript);
                    osw.close();

                    Process process = Runtime.getRuntime().exec("wscript //nologo tts.vbs");
                    process.waitFor();

                    file.delete();
                } catch (Exception exception) {
                    exception.printStackTrace();
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
                ConnectServer.msg("Ocorreu um erro ao gerar um beep! (" + exception.getMessage() + ")");
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
                ServerGUI.getInstance().audioControlPanel();
            } else {
                if (input.equalsIgnoreCase("receive")) {
                    if (ServerGUI.AUDIO_USER != null) {
                        if (ServerAudioComponents.AUDIO_USER) {
                            ServerAudioComponents.AUDIO_USER = false;
                            ServerGUI.AUDIO_USER.setBackground(Color.RED);
                        } else {
                            ServerGUI.AUDIO_USER.setBackground(Color.YELLOW);
                        }
                    }
                } else if (input.equalsIgnoreCase("send")) {
                    if (ServerGUI.AUDIO_SERVER != null) {
                        if (ServerAudioComponents.AUDIO_SERVER) {
                            ServerAudioComponents.AUDIO_SERVER = false;
                            ServerGUI.AUDIO_SERVER.setBackground(Color.RED);
                        } else {
                            ServerGUI.AUDIO_SERVER.setBackground(Color.YELLOW);
                        }
                    }
                }
                client.send("audio " + input);
            }
        }
    }

}

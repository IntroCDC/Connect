package br.com.introcdc.connect.server;
/*
 * Written by IntroCDC, Bruno Coêlho at 13/01/2025 - 13:47
 */

import br.com.introcdc.connect.Connect;
import br.com.introcdc.connect.server.command.ServerCommand;
import br.com.introcdc.connect.server.command.ServerCommandEnum;
import br.com.introcdc.connect.server.components.ServerAudioComponents;
import br.com.introcdc.connect.server.components.ServerControlComponents;
import br.com.introcdc.connect.server.components.ServerFileComponents;
import br.com.introcdc.connect.server.connection.ClientHandler;
import br.com.introcdc.connect.server.connection.SocketKeepAlive;
import br.com.introcdc.connect.server.gui.ServerGUI;

import java.io.File;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class ConnectServer {

    public static void main(String[] args) {
        startServer();
    }

    public static final ScheduledExecutorService EXECUTOR =
            Executors.newScheduledThreadPool(10);

    public static final Map<Integer, ClientHandler> CLIENTS = new HashMap<>();
    public static final List<Integer> TESTING = new ArrayList<>();
    public static int SELECTED_CLIENT = 0;
    public static List<String> CONNECTED_KEYS = new ArrayList<>();
    public static boolean DISCONNECT_DUPLICATE = false;
    public static final AtomicLong BYTES_SENT = new AtomicLong(0);
    public static final AtomicLong BYTES_RECEIVED = new AtomicLong(0);

    public static void handleCommand(String command) {
        String lower = command.toLowerCase();

        if (ServerCommand.handleCommand(lower)) {
            return;
        }
        if (SELECTED_CLIENT == 0) {
            msg("Cliente não selecionado!");
        } else {
            if (lower.startsWith("send ")) {
                ServerFileComponents.sendFile(command.substring(5));
            } else if (lower.startsWith("chat ")) {
                ClientHandler.handleChat(command.substring(5));
            } else if (lower.startsWith("audio ")) {
                ServerAudioComponents.handleAudio(command.substring(6));
            } else {
                if (SELECTED_CLIENT == -1) {
                    if (CLIENTS.isEmpty()) {
                        msg("Não possui nenhum cliente conectado no momento!");
                    } else {
                        msg("Enviando o comando \"" + command + "\" para " + CLIENTS.size() + " clientes!");
                        for (ClientHandler clientHandler : new ArrayList<>(CLIENTS.values())) {
                            clientHandler.send(command);
                        }
                    }
                } else {
                    ClientHandler client = CLIENTS.get(SELECTED_CLIENT);
                    client.send(command);
                }
            }
        }
    }

    public static void msg(String message) {
        msg(message, true);
    }

    public static void msg(String message, boolean fast) {
        ServerGUI.log(message);
    }

    public static void startServer() {
        new File("connect").mkdir();
        ServerGUI.showGUI();
        ServerCommandEnum.registerCommands();
        new Thread(ServerControlComponents::startControlServer).start();
        new Thread(ConnectServer::startServerConsole).start();

        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(Connect.PORT)) {
                msg("Servidor iniciado em " + Connect.IP + ":" + Connect.PORT, false);
                new Thread(() -> ServerGUI.getInstance().monitorTraffic()).start();
                while (true) {
                    int id = 1;
                    while (CLIENTS.containsKey(id) || TESTING.contains(id)) {
                        id++;
                    }
                    TESTING.add(id);
                    Socket clientSocket = serverSocket.accept();
                    ClientHandler handler = new ClientHandler(clientSocket, id);
                    new Thread(new SocketKeepAlive(handler)).start();
                    new Thread(handler).start();

                    CLIENTS.put(id, handler);
                    ServerGUI.addClient(String.valueOf(id));
                    int removeId = id;
                    EXECUTOR.schedule(() -> TESTING.remove(removeId), 1, TimeUnit.SECONDS);
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }).start();
    }

    public static void startServerConsole() {
        Scanner scanner = new Scanner(System.in);
        if (ServerGUI.getInstance() == null) {
            msg("Para ajuda com comandos, digite: help");
        }
        while (true) {
            String command = scanner.nextLine();
            handleCommand(command);
        }
    }

}

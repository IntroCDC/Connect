package br.com.introcdc.connect.server.connection;
/*
 * Written by IntroCDC, Bruno Coêlho at 15/01/2025 - 18:09
 */

import java.net.Socket;

public class SocketKeepAlive implements Runnable {

    private Socket socket = null;
    private ClientHandler handler;

    public SocketKeepAlive(Socket socket) {
        this.socket = socket;
    }

    public SocketKeepAlive(ClientHandler handler) {
        this.handler = handler;
    }

    public Socket getSocket() {
        return socket;
    }

    public ClientHandler getHandler() {
        return handler;
    }

    @Override
    public void run() {
        while (getSocket() != null && getSocket().isConnected()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {
            }
        }

        while (getHandler() != null && getHandler().getClientSocket().isConnected()) {
            if (getHandler() != null) {
                getHandler().send("ignore");
            }
            try {
                Thread.sleep(60 * 1000);
            } catch (InterruptedException ignored) {
            }
        }
    }

}

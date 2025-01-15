package br.com.introcdc.connect.client.components.settings;
/*
 * Written by IntroCDC, Bruno Coêlho at 15/01/2025 - 16:26
 */

import java.io.File;

public class FileInfo {

    private final File file;
    private final int index;

    public FileInfo(File file, int index) {
        this.file = file;
        this.index = index;
    }

    public File getFile() {
        return file;
    }

    public int getIndex() {
        return index;
    }

}

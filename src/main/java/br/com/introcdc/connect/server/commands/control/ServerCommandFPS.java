package br.com.introcdc.connect.server.commands.control;
/*
 * Written by IntroCDC, Bruno Coêlho at 15/01/2025 - 18:42
 */

import br.com.introcdc.connect.server.ConnectServerGUI;
import br.com.introcdc.connect.server.command.ServerCommand;
import br.com.introcdc.connect.server.components.ServerImageComponents;

public class ServerCommandFPS extends ServerCommand {

    public ServerCommandFPS() {
        super("fps");
    }

    @Override
    public void execute(String command, String input) throws Exception {
        try {
            int fps = Integer.parseInt(command.split(" ")[1]);
            if (fps <= 0 || fps > 60) {
                msg("Digite um FPS entre 1 e 60!");

            } else {
                msg("FPS definido: " + fps);
                ServerImageComponents.FPS = fps;
            }
            ConnectServerGUI.fpsButton.setText("FPS (" + ServerImageComponents.FPS + ")");
        } catch (Exception ignored) {
            msg("Digite um número válido!");
        }
    }

}

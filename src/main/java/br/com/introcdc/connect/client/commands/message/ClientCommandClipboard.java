package br.com.introcdc.connect.client.commands.message;
/*
 * Written by IntroCDC, Bruno Coêlho at 15/01/2025 - 17:39
 */

import br.com.introcdc.connect.client.command.ClientCommand;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;

public class ClientCommandClipboard extends ClientCommand {

    public ClientCommandClipboard() {
        super("clipboard");
    }

    @Override
    public void execute(String command, String input) throws Exception {
        if (input.isEmpty()) {
            msg("Digite algo para definir na clipboard!");
            return;
        }
        if (input.equalsIgnoreCase("<<<")) {
            try {
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                if (clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
                    msg("Texto na clipboard: '" + clipboard.getData(DataFlavor.stringFlavor) + "'");
                } else {
                    msg("A clipboard atual não possui uma string!");
                }
            } catch (Exception exception) {
                msg("Não foi encontrado nada na clipboard!");
                exception(exception);
            }
        } else {
            try {
                StringSelection stringSelection = new StringSelection(input);
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(stringSelection, null);
                msg("Texto definido na clipboard: '" + input + "'");
            } catch (Exception exception) {
                msg("Ocorreu um erro ao definir a string " + input + " na clipboard! (" + exception.getMessage() + ")");
                exception(exception);
            }
        }
    }

}

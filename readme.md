# Análise do Projeto "Connect"

## 📝 Resumo do Projeto
O projeto **Connect** é uma ferramenta de administração remota (conhecida como **RAT** - *Remote Administration Tool* ou *Remote Access Trojan*). Ele possui uma arquitetura clássica de Cliente e Servidor, onde o "Servidor" controla remotamente uma ou mais máquinas "Clientes" que executam o código cliente.

Pela análise dos arquivos, a ferramenta é bastante abrangente e inclui integrações disfarçadas como plugins para servidores de Minecraft (Bukkit, Bungee, Fabric, Sponge, Velocity), o que sugere um possível vetor de distribuição.

> [!CAUTION]
> Este projeto contém diversas funcionalidades intrusivas, incluindo ataques de negação de serviço (DDoS), keyloggers, acesso a webcam e manipulação de arquivos do sistema. Dependendo do uso, ele pode ser classificado como um software malicioso (Malware/Trojan).

## ⚙️ Principais Funções

O projeto é dividido em diversos módulos de comandos que podem ser executados remotamente. Suas principais funções são:

* **Controle e Espionagem:** 
  * Captura de tela e webcam (`ClientCommandScreenWebcam`).
  * Controle e monitoramento de teclado e mouse (`ClientCommandKeyboardType`, `ClientCommandMouseClick`, `ClientKeyLoggerComponents`).
  * Interceptação de área de transferência (Clipboard).
* **Gestão de Arquivos (File Manager):** 
  * Upload, download, cópia, exclusão, compactação e descompactação de arquivos.
  * Capacidades destrutivas (`ClientCommandDestroyEverything`).
* **Processos e Sistema:**
  * Execução, listagem e finalização de processos.
  * Alteração do papel de parede do sistema.
  * Interação por voz e caixas de diálogo no computador alvo (`ClientCommandVoice`, `ClientCommandAsk`).
* **Ataques de Rede:**
  * Módulo para execução de ataques DDoS (`ClientCommandDDOS`, `UDPFlood`).

---

## 📂 Lista de Arquivos `.java` (Categorizados)

### 📌 Base do Projeto
* `.\src\main\java\br\com\introcdc\connect\Connect.java` (Classe principal)

### 💻 Módulo Cliente (`br.com.introcdc.connect.client`)
É a parte que roda na máquina alvo e executa os comandos recebidos.

**Base do Cliente e Servidores (Plugins de Minecraft):**
* `ConnectClient.java`
* `server\BukkitMain.java`
* `server\BungeeMain.java`
* `server\FabricMain.java`
* `server\SpongeMain.java`
* `server\VelocityMain.java`
* `remote\RemoteEvent.java`

**Comandos de Áudio e Controle (Mouse/Teclado/DDoS):**
* `command\ClientCommand.java` e `ClientCommandEnum.java`
* `commands\audio\ClientCommandAudio.java`
* `commands\control\ClientCommandDDOS.java`
* `commands\control\ClientCommandFunctions.java`
* `commands\control\ClientCommandKeyboardType.java`
* `commands\control\ClientCommandMouseClick.java`
* `commands\control\ClientCommandMouseScroll.java`
* `commands\control\ClientCommandWallpaper.java`

**Comandos de Arquivos (Navegação, Manipulação e Transferência):**
* `commands\file\external\ClientCommandDestroyEverything.java`
* `commands\file\external\ClientCommandDownload.java`
* `commands\file\external\ClientCommandReceive.java`
* `commands\file\external\ClientCommandSend.java`
* `commands\file\external\ClientCommandSshKey.java`
* `commands\file\manipulate\ClientCommandCopy.java`
* `commands\file\manipulate\ClientCommandDel.java`
* `commands\file\manipulate\ClientCommandMakeDir.java`
* `commands\file\manipulate\ClientCommandMove.java`
* `commands\file\manipulate\ClientCommandUnzip.java`
* `commands\file\manipulate\ClientCommandZip.java`
* `commands\file\navigation\ClientCommandEnterFolder.java`
* `commands\file\navigation\ClientCommandFileInfo.java`
* `commands\file\navigation\ClientCommandFindFile.java`
* `commands\file\navigation\ClientCommandListFiles.java`
* `commands\file\navigation\ClientCommandOpen.java`
* `commands\file\navigation\ClientCommandView.java`

**Comandos de Imagem e Tela:**
* `commands\image\ClientCommandHistory.java`
* `commands\image\ClientCommandLiveStopper.java`
* `commands\image\ClientCommandQuality.java`
* `commands\image\ClientCommandScreenWebcam.java`

**Comandos de Informação e Sistema:**
* `commands\info\ClientCommandClose.java`, `ClientCommandConsole.java`, `ClientCommandDebug.java`
* `commands\info\ClientCommandGC.java`, `ClientCommandIgnore.java`, `ClientCommandInfo.java`
* `commands\info\ClientCommandPing.java`, `ClientCommandRestart.java`, `ClientCommandUninstall.java`

**Mensagens e Keylogger:**
* `commands\message\ClientCommandAsk.java`, `ClientCommandChat.java`
* `commands\message\ClientCommandClipboard.java`, `ClientCommandKeyLogger.java`
* `commands\message\ClientCommandMessage.java`, `ClientCommandVoice.java`

**Gestão de Processos:**
* `commands\process\ClientCommandExec.java`, `ClientCommandKill.java`
* `commands\process\ClientCommandListProcess.java`, `ClientCommandLog.java`, `ClientCommandProcess.java`

**Componentes (Lógica interna do Cliente):**
* `components\ClientAudioComponents.java`, `ClientChatComponents.java`, `ClientControlComponents.java`
* `components\ClientFileComponents.java`, `ClientImageComponents.java`, `ClientInstallComponents.java`
* `components\ClientKeyLoggerComponents.java`, `ClientProcessComponents.java`
* `components\settings\FileInfo.java`, `UDPFlood.java`

---

### 🖥️ Módulo Servidor (`br.com.introcdc.connect.server`)
É a parte que o administrador utiliza para controlar remotamente os clientes.

**Base e Comandos do Servidor:**
* `ConnectServer.java`
* `command\ServerCommand.java`, `ServerCommandEnum.java`
* `commands\control\ServerCommandFPS.java`, `ServerCommandFunctionsPanel.java`
* `commands\control\ServerCommandKeyboardControl.java`, `ServerCommandMouse.java`
* `commands\control\ServerCommandMouseMove.java`, `ServerCommandMouseMoveClick.java`
* `commands\control\ServerCommandServerControl.java`
* `commands\info\ServerCommandDeselect.java`, `ServerCommandDuplicate.java`
* `commands\info\ServerCommandHelp.java`, `ServerCommandListClients.java`, `ServerCommandSelect.java`

**Componentes e Conexão:**
* `components\ServerAudioComponents.java`, `ServerControlComponents.java`
* `components\ServerFileComponents.java`, `ServerImageComponents.java`
* `components\settings\FileInfo.java`
* `connection\ClientHandler.java`, `SocketKeepAlive.java`

**Interface Gráfica (GUI):**
* `gui\InteractiveImagePanel.java`
* `gui\ServerGUI.java`

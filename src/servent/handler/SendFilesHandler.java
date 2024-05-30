package servent.handler;

import app.AppConfig;
import servent.message.Message;

public class SendFilesHandler implements MessageHandler{

    private Message clientMessage;

    public SendFilesHandler(Message clientMessage) {
        this.clientMessage = clientMessage;
    }

    @Override
    public void run() {
        AppConfig.timestampedStandardPrint(clientMessage.getSenderPort() + ": " + clientMessage.getMessageText());
    }
}

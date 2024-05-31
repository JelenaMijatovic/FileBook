package servent.handler;

import app.AppConfig;
import servent.message.Message;
import servent.message.MessageType;

public class SendFilesHandler implements MessageHandler{

    private Message clientMessage;

    public SendFilesHandler(Message clientMessage) {
        this.clientMessage = clientMessage;
    }

    @Override
    public void run() {
        if (clientMessage.getMessageType() == MessageType.SEND) {
            AppConfig.timestampedStandardPrint(clientMessage.getSenderPort() + ": " + clientMessage.getMessageText());
        } else {
            AppConfig.timestampedErrorPrint("Send files handler got a message that is not SEND");
        }
    }
}

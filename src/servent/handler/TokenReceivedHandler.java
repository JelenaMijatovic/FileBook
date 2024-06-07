package servent.handler;

import app.AppConfig;
import servent.message.Message;
import servent.message.MessageType;

public class TokenReceivedHandler implements MessageHandler {

    private Message clientMessage;

    public TokenReceivedHandler(Message clientMessage) {
        this.clientMessage = clientMessage;
    }

    @Override
    public void run() {
        if (clientMessage.getMessageType() == MessageType.TOK_REC) {
            AppConfig.hasToken.set(false);
            AppConfig.timestampedStandardPrint(clientMessage.getSenderPort() + " successfully received our token");
        } else {
            AppConfig.timestampedErrorPrint("Token received handler got a message that is not TOK_REC");
        }
    }
}

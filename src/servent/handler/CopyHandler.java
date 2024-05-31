package servent.handler;

import app.AppConfig;
import servent.message.Message;
import servent.message.MessageType;

public class CopyHandler implements MessageHandler {

    private Message clientMessage;

    public CopyHandler(Message clientMessage) {
        this.clientMessage = clientMessage;
    }

    @Override
    public void run() {
        if (clientMessage.getMessageType() == MessageType.COPY) {
            String path = clientMessage.getMessageText();
            AppConfig.chordState.backupFile(path, clientMessage.getSenderPort());
        } else {
            AppConfig.timestampedErrorPrint("Copy handler got a message that is not COPY");
        }
    }

}

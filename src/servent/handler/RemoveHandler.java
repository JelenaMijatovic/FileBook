package servent.handler;

import app.AppConfig;
import servent.message.Message;
import servent.message.MessageType;

public class RemoveHandler implements MessageHandler{

    private Message clientMessage;

    public RemoveHandler(Message clientMessage) {
        this.clientMessage = clientMessage;
    }

    @Override
    public void run() {
        if (clientMessage.getMessageType() == MessageType.REMOVE) {
            AppConfig.chordState.removeBackup(clientMessage.getMessageText(), clientMessage.getSenderPort());
        } else {
            AppConfig.timestampedErrorPrint("Remove files handler got a message that is not REMOVE");
        }
    }
}

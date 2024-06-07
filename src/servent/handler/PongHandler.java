package servent.handler;

import app.AppConfig;
import servent.message.Message;
import servent.message.MessageType;

public class PongHandler implements MessageHandler{

    private Message clientMessage;

    public PongHandler(Message clientMessage) {
        this.clientMessage = clientMessage;
    }

    @Override
    public void run() {
        if (clientMessage.getMessageType() == MessageType.PONG) {
            for (int i = 0; i < 2; i++) {
                if (AppConfig.chordState.getBackupSuccessors()[i] == clientMessage.getSenderPort()) {
                    AppConfig.chordState.getBackupLateCount()[i] = 0;
                }
            }
            //AppConfig.chordState.getBackupSuccessors().put(clientMessage.getSenderPort(), 0);
            //If node down pred + nextnode
        } else {
            AppConfig.timestampedErrorPrint("Pong handler got a message that is not PONG");
        }
    }
}

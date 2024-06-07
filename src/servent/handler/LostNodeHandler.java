package servent.handler;

import app.AppConfig;
import app.ServentInfo;
import servent.message.Message;
import servent.message.MessageType;

public class LostNodeHandler implements MessageHandler{

    private Message clientMessage;

    public LostNodeHandler(Message clientMessage) {
        this.clientMessage = clientMessage;
    }

    @Override
    public void run() {
        if (clientMessage.getMessageType() == MessageType.LOST) {
            int port;
            try {
                port = Integer.parseInt(clientMessage.getMessageText());
                if (AppConfig.chordState.getPredecessor().getListenerPort() == port) {
                    AppConfig.timestampedStandardPrint("Switching predecessor " + port);
                    for (ServentInfo si : AppConfig.chordState.getAllNodeInfo()) {
                        if (si.getListenerPort() == clientMessage.getSenderPort()) {
                            AppConfig.chordState.setPredecessor(si);
                            break;
                        }
                    }
                    AppConfig.chordState.takeOverFilesFromBackup(port);
                }
                AppConfig.chordState.removeNode(port);
            } catch (NumberFormatException e) {
                AppConfig.timestampedErrorPrint("Got lost node message with bad text: " + clientMessage.getMessageText());
            }
        } else {
            AppConfig.timestampedErrorPrint("Lost node handler got a message that is not LOST");
        }
    }
}

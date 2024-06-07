package servent.handler;

import app.AppConfig;
import servent.message.Message;
import servent.message.MessageType;

public class SuspectHandler implements MessageHandler {

    private Message clientMessage;

    public SuspectHandler(Message clientMessage) {
        this.clientMessage = clientMessage;
    }

    @Override
    public void run() {
        if (clientMessage.getMessageType() == MessageType.SUS) {
            int port;
            try {
                port = Integer.parseInt(clientMessage.getMessageText());

                AppConfig.chordState.ping(port);
            } catch (NumberFormatException e) {
                AppConfig.timestampedErrorPrint("Got suspect message with bad text: " + clientMessage.getMessageText());
            }
        } else {
            AppConfig.timestampedErrorPrint("Suspect handler got a message that is not SUS");
        }
    }
}

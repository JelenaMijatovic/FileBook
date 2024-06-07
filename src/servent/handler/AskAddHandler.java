package servent.handler;

import app.AppConfig;
import servent.message.Message;
import servent.message.MessageType;

public class AskAddHandler implements MessageHandler {
    private Message clientMessage;

    public AskAddHandler(Message clientMessage) {
        this.clientMessage = clientMessage;
    }

    @Override
    public void run() {
        if (clientMessage.getMessageType() == MessageType.ASK_ADD) {
            String[] splitText = clientMessage.getMessageText().split(",");
            if (splitText.length == 3) {
                int key;
                String path;
                int visibility;
                try {
                    key = Integer.parseInt(splitText[0]);
                    path = splitText[1];
                    visibility = Integer.parseInt(splitText[2]);

                    AppConfig.chordState.askAddFile(key, path, visibility);
                } catch (NumberFormatException e) {
                    AppConfig.timestampedErrorPrint("Got ask add message with bad text: " + clientMessage.getMessageText());
                }
            } else {
                AppConfig.timestampedErrorPrint("Got ask add message with bad text: " + clientMessage.getMessageText());
            }
        } else {
            AppConfig.timestampedErrorPrint("Ask add handler got a message that is not ASK_ADD");
        }
    }
}

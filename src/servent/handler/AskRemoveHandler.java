package servent.handler;

import app.AppConfig;
import servent.message.Message;
import servent.message.MessageType;

public class AskRemoveHandler implements MessageHandler{

    private Message clientMessage;

    public AskRemoveHandler(Message clientMessage) {
        this.clientMessage = clientMessage;
    }

    @Override
    public void run() {
        if (clientMessage.getMessageType() == MessageType.ASK_REMOVE) {
            String[] splitText = clientMessage.getMessageText().split(",");
            if (splitText.length == 2) {
                int key;
                String path;
                try {
                    key = Integer.parseInt(splitText[0]);
                    path = splitText[1];

                    AppConfig.chordState.askRemoveFile(key, path);
                } catch (NumberFormatException e) {
                    AppConfig.timestampedErrorPrint("Got ask remove message with bad text: " + clientMessage.getMessageText());
                }
            } else {
                AppConfig.timestampedErrorPrint("Got ask remove message with bad text: " + clientMessage.getMessageText());
            }
        } else {
            AppConfig.timestampedErrorPrint("Ask remove handler got a message that is not ASK_REMOVE");
        }
    }
}

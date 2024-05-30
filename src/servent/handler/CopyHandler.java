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
            String[] splitText = clientMessage.getMessageText().split(",");
            if (splitText.length == 3) {
                String path;
                int visibility;
                int count;

                try {
                    path = splitText[0];
                    visibility = Integer.parseInt(splitText[1]);
                    count = Integer.parseInt(splitText[2]);

                    AppConfig.chordState.addFile(path, visibility, count);
                } catch (NumberFormatException e) {
                    AppConfig.timestampedErrorPrint("Got copy message with bad text: " + clientMessage.getMessageText());
                }
            } else {
                AppConfig.timestampedErrorPrint("Got copy message with bad text: " + clientMessage.getMessageText());
            }

        } else {
            AppConfig.timestampedErrorPrint("Put handler got a message that is not COPY");
        }
    }

}

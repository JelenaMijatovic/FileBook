package servent.handler;

import app.AppConfig;
import servent.message.Message;
import servent.message.MessageType;

public class TokenNoticeHandler implements MessageHandler {

    private Message clientMessage;

    public TokenNoticeHandler(Message clientMessage) {
        this.clientMessage = clientMessage;
    }

    @Override
    public void run() {
        if (clientMessage.getMessageType() == MessageType.TOK_NOC) {
            int hasToken;
            try {
                hasToken = Integer.parseInt(clientMessage.getMessageText());
                if (hasToken == 1) {
                    AppConfig.chordState.setNeighbourWithToken(clientMessage.getSenderPort());
                } else if (hasToken == 0) {
                    AppConfig.chordState.setNeighbourWithToken(0);
                }
            } catch (NumberFormatException e) {
                AppConfig.timestampedErrorPrint("Got token notice message with bad text: " + clientMessage.getMessageText());
            }
        } else {
            AppConfig.timestampedErrorPrint("Token notice handler got a message that is not TOK_NOC");
        }
    }
}

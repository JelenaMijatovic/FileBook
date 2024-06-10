package servent.handler;

import app.AppConfig;
import app.SKToken;
import servent.message.*;
import servent.message.util.MessageUtil;

import java.util.Objects;

public class TokenSenderHandler implements MessageHandler{

    private Message clientMessage;

    public TokenSenderHandler(Message clientMessage) {
        this.clientMessage = clientMessage;
    }

    @Override
    public void run() {
        if (clientMessage.getMessageType() == MessageType.TOK_SEND) {
            String[] splitText = clientMessage.getMessageText().split(":");
            if (splitText.length == 3) {
                int requesterId;
                String lastRequests;
                String queue;
                try {
                    requesterId = Integer.parseInt(splitText[0]);
                    lastRequests = splitText[1];
                    queue = splitText[2];
                    SKToken token = new SKToken();
                    token.buildTokenfromString(lastRequests, queue);
                    AppConfig.token = token;
                    AppConfig.hasToken.set(true);
                    TokenReceivedMessage trm = new TokenReceivedMessage(AppConfig.myServentInfo.getListenerPort(), clientMessage.getSenderPort());
                    MessageUtil.sendMessage(trm);
                    AppConfig.chordState.setNeighbourWithToken(0);
                    if (requesterId == AppConfig.myId) {
                        AppConfig.timestampedStandardPrint("Got token!");
                    } else if (requesterId > 9000) {
                        AppConfig.timestampedStandardPrint("Got token in emergency!");
                    } else {
                        TokenSendMessage tsm = new TokenSendMessage(AppConfig.myServentInfo.getListenerPort(), AppConfig.chordState.getNextNodePort(), requesterId, lastRequests + ":" + queue);
                        MessageUtil.sendMessage(tsm);
                    }
                } catch (NumberFormatException e) {
                    AppConfig.timestampedErrorPrint("Got token sender message with bad text: " + clientMessage.getMessageText());
                }
            } else {
                AppConfig.timestampedErrorPrint("Got token sender message with bad text: " + clientMessage.getMessageText());
            }
        } else {
            AppConfig.timestampedErrorPrint("Token sender handler got a message that is not TOK_SEND");
        }
    }
}

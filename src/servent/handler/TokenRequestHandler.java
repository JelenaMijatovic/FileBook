package servent.handler;

import app.AppConfig;
import servent.message.*;
import servent.message.util.MessageUtil;

import java.util.Objects;

public class TokenRequestHandler implements MessageHandler{

    private Message clientMessage;

    public TokenRequestHandler(Message clientMessage) {
        this.clientMessage = clientMessage;
    }

    @Override
    public void run() {
        if (clientMessage.getMessageType() == MessageType.TOK_REQ) {
            String[] splitText = clientMessage.getMessageText().split(",");
            if (splitText.length == 3) {
                int requesterId;
                int sequenceNumber;
                String direction;
                try {
                    requesterId = Integer.parseInt(splitText[0]);
                    sequenceNumber = Integer.parseInt(splitText[1]);
                    direction = splitText[2];
                    if (requesterId != AppConfig.myId) {
                        if (sequenceNumber > AppConfig.requests[requesterId])
                            AppConfig.requests[requesterId] = sequenceNumber;
                        if (AppConfig.hasToken.get() && !AppConfig.inCriticalSection.get() && Objects.equals(AppConfig.requests[requesterId], AppConfig.token.getLastRequests()[requesterId] + 1)) {
                            AppConfig.timestampedStandardPrint("Sending token!");
                            TokenSendMessage tsm = new TokenSendMessage(AppConfig.myServentInfo.getListenerPort(), clientMessage.getSenderPort(), requesterId, AppConfig.token.toString());
                            MessageUtil.sendMessage(tsm);
                            AppConfig.hasToken.set(false);
                            TokenNoticeMessage tnm = new TokenNoticeMessage(AppConfig.myServentInfo.getListenerPort(), AppConfig.chordState.getNextNodePort(), 0);
                            MessageUtil.sendMessage(tnm);
                            tnm = new TokenNoticeMessage(AppConfig.myServentInfo.getListenerPort(), AppConfig.chordState.getPredecessor().getListenerPort(), 0);
                            MessageUtil.sendMessage(tnm);
                        } else if (!AppConfig.hasToken.get()) {
                            TokenRequestMessage trm;
                            if (Objects.equals(direction, "F")) {
                                trm = new TokenRequestMessage(clientMessage.getSenderPort(), AppConfig.chordState.getNextNodePort(), requesterId, sequenceNumber, "F");
                            } else {
                                trm = new TokenRequestMessage(clientMessage.getSenderPort(), AppConfig.chordState.getPredecessor().getListenerPort(), requesterId, sequenceNumber, "B");
                            }
                            MessageUtil.sendMessage(trm);
                        }
                    }
                } catch (NumberFormatException e) {
                    AppConfig.timestampedErrorPrint("Got token request message with bad text: " + clientMessage.getMessageText());
                }
            } else {
                AppConfig.timestampedErrorPrint("Got token request message with bad text: " + clientMessage.getMessageText());
            }
        } else {
            AppConfig.timestampedErrorPrint("Token request handler got a message that is not TOK_REQ");
        }
    }
}

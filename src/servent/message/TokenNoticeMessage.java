package servent.message;

public class TokenNoticeMessage extends BasicMessage{

    public TokenNoticeMessage(int senderPort, int receiverPort, int hasToken) {
        super(MessageType.TOK_NOC, senderPort, receiverPort, String.valueOf(hasToken));
    }
}

package servent.message;

public class TokenReceivedMessage extends BasicMessage {
    public TokenReceivedMessage(int senderPort, int receiverPort) {
        super(MessageType.TOK_REC, senderPort, receiverPort);
    }
}

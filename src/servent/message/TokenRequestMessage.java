package servent.message;

public class TokenRequestMessage extends BasicMessage {
    public TokenRequestMessage(int senderPort, int receiverPort, Integer requesterId, int sequenceNumber, String direction) {
        super(MessageType.TOK_REQ, senderPort, receiverPort, requesterId + "," + sequenceNumber + "," + direction);
    }
}

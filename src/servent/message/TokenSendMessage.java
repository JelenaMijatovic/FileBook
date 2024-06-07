package servent.message;

public class TokenSendMessage extends BasicMessage {

    public TokenSendMessage(int senderPort, int receiverPort, int requesterId, String token) {
        super(MessageType.TOK_SEND, senderPort, receiverPort, requesterId + ":" + token);
    }
}

package servent.message;

public class LostNodeMessage extends BasicMessage {

    public LostNodeMessage(int senderPort, int receiverPort, int lostPort) {
        super(MessageType.LOST, senderPort, receiverPort, String.valueOf(lostPort));
    }
}

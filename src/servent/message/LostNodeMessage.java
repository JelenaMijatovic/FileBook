package servent.message;

public class LostNodeMessage extends BasicMessage {

    public LostNodeMessage(int senderPort, int receiverPort, String lostPort) {
        super(MessageType.LOST, senderPort, receiverPort, String.valueOf(lostPort));
    }
}

package servent.message;

public class LostNodeMessage extends BasicMessage {

    public LostNodeMessage(int senderPort, int receiverPort, String lostPort, String direction) {
        super(MessageType.LOST, senderPort, receiverPort, lostPort + ":" + direction);
    }
}

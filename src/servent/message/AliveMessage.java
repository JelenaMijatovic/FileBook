package servent.message;

public class AliveMessage extends BasicMessage {

    public AliveMessage(int senderPort, int receiverPort, String messageText) {
        super(MessageType.ALIVE, senderPort, receiverPort, messageText);
    }
}

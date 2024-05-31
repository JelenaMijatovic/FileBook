package servent.message;

public class RemoveMessage extends BasicMessage {

    public RemoveMessage(int senderPort, int receiverPort, String path) {
        super(MessageType.REMOVE, senderPort, receiverPort, path);
    }
}

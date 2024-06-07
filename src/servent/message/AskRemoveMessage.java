package servent.message;

public class AskRemoveMessage extends BasicMessage {
    public AskRemoveMessage(int senderPort, int receiverPort, int key, String path) {
        super(MessageType.ASK_REMOVE, senderPort, receiverPort, key + "," + path);
    }
}

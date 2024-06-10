package servent.message;

public class AskAddMessage extends BasicMessage {

    public AskAddMessage(int senderPort, int receiverPort, int key, String path, int visibility) {
        super(MessageType.ASK_ADD, senderPort, receiverPort, key + "," + path + "," + visibility);
    }
}

package servent.message;

public class CopyMessage extends BasicMessage {

    public CopyMessage(int senderPort, int receiverPort, String path) {
        super(MessageType.COPY, senderPort, receiverPort, path);
    }
}

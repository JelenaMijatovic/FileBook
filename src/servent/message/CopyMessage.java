package servent.message;

public class CopyMessage extends BasicMessage {

    public CopyMessage(int senderPort, int receiverPort, String path, int visibility, int count) {
        super(MessageType.COPY, senderPort, receiverPort, path + "," + visibility + "," + count);
    }
}

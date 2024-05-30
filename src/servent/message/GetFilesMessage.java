package servent.message;

public class GetFilesMessage extends BasicMessage {

    public GetFilesMessage(int senderPort, int receiverPort, int originalSenderPort, int requestedPort) {
        super(MessageType.GET, senderPort, receiverPort, originalSenderPort + "," + requestedPort);
    }
}

package servent.message;

public class SuspectMessage extends BasicMessage {

    public SuspectMessage(int senderPort, int receiverPort, int suspectPort) {
        super(MessageType.SUS, senderPort, receiverPort, String.valueOf(suspectPort));
    }
}

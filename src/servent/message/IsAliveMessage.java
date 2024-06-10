package servent.message;

public class IsAliveMessage extends BasicMessage {
    public IsAliveMessage(int senderPort, int receiverPort, Integer originalPort) {
        super(MessageType.IS_ALIVE, senderPort, receiverPort, String.valueOf(originalPort));
    }
}

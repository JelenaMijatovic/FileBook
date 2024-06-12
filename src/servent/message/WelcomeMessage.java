package servent.message;

import java.util.Map;

public class WelcomeMessage extends BasicMessage {

	private static final long serialVersionUID = -8981406250652693908L;
	
	public WelcomeMessage(int senderPort, int receiverPort) {
		super(MessageType.WELCOME, senderPort, receiverPort);

	}
}

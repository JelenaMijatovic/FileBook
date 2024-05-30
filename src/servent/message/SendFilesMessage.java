package servent.message;

import java.util.List;

public class SendFilesMessage extends BasicMessage{

    public SendFilesMessage(int senderPort, int receiverPort, String files) {
        super(MessageType.SEND, senderPort, receiverPort, files);
    }
}

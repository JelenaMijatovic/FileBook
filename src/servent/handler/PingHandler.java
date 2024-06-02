package servent.handler;

import app.AppConfig;
import servent.message.Message;
import servent.message.MessageType;

public class PingHandler implements MessageHandler {

    private Message clientMessage;

    public PingHandler(Message clientMessage) {
        this.clientMessage = clientMessage;
    }

    @Override
    public void run() {
        if (clientMessage.getMessageType() == MessageType.PING) {
            //set sender as active on this servent? If node down pred + nextnode
        } else {
            AppConfig.timestampedErrorPrint("Ping handler got a message that is not PING");
        }
    }
}

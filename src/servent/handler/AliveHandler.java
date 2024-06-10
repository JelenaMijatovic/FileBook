package servent.handler;

import app.AppConfig;
import servent.message.AliveMessage;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.util.MessageUtil;

public class AliveHandler implements MessageHandler{

    private Message clientMessage;

    public AliveHandler(Message clientMessage) {
        this.clientMessage = clientMessage;
    }

    @Override
    public void run() {
        if (clientMessage.getMessageType() == MessageType.ALIVE) {
            if (clientMessage.getMessageText().isEmpty()) {
                AppConfig.chordState.getSuspects().put(clientMessage.getSenderPort(), AppConfig.chordState.getSuspects().get(clientMessage.getSenderPort()) - 1);
            } else {
                int port;
                port = Integer.parseInt(clientMessage.getMessageText());
                AliveMessage am = new AliveMessage(clientMessage.getSenderPort(), port, null);
                MessageUtil.sendMessage(am);
            }
        } else {
            AppConfig.timestampedErrorPrint("Alive handler got a message that is not ALIVE");
        }
    }
}

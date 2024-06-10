package servent.handler;

import app.AppConfig;
import servent.message.AliveMessage;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.util.MessageUtil;

public class IsAliveHandler implements MessageHandler{

    private Message clientMessage;

    public IsAliveHandler(Message clientMessage) {
        this.clientMessage = clientMessage;
    }

    @Override
    public void run() {
        if (clientMessage.getMessageType() == MessageType.IS_ALIVE) {
            AliveMessage am = new AliveMessage(AppConfig.myServentInfo.getListenerPort(), clientMessage.getSenderPort(), clientMessage.getMessageText());
            MessageUtil.sendMessage(am);
        } else {
            AppConfig.timestampedErrorPrint("Is alive handler got a message that is not IS_ALIVE");
        }
    }
}

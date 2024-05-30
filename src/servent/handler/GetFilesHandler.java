package servent.handler;

import app.AppConfig;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.SendFilesMessage;
import servent.message.util.MessageUtil;

import java.util.List;

public class GetFilesHandler implements MessageHandler{

    private Message clientMessage;

    public GetFilesHandler(Message clientMessage) {
        this.clientMessage = clientMessage;
    }

    @Override
    public void run() {
        if (clientMessage.getMessageType() == MessageType.GET) {
            String[] splitText = clientMessage.getMessageText().split(",");
            if (splitText.length == 2) {
                int senderPort;
                int requestedPort;

                try {
                    senderPort = Integer.parseInt(splitText[0]);
                    requestedPort = Integer.parseInt(splitText[1]);

                    if (AppConfig.myServentInfo.getListenerPort() == requestedPort) {
                        String files = AppConfig.chordState.listFiles(senderPort);
                        SendFilesMessage sfm = new SendFilesMessage(AppConfig.myServentInfo.getListenerPort(), senderPort, files);
                        MessageUtil.sendMessage(sfm);
                    } else {
                        AppConfig.chordState.getFiles(senderPort, requestedPort);
                    }
                } catch (NumberFormatException e) {
                    AppConfig.timestampedErrorPrint("Got get files message with bad text: " + clientMessage.getMessageText());
                }
            } else {
                AppConfig.timestampedErrorPrint("Got get files message with bad text: " + clientMessage.getMessageText());
            }

        } else {
            AppConfig.timestampedErrorPrint("Get files handler got a message that is not GET");
        }
    }
}

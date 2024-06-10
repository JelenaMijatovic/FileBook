package servent.handler;

import app.AppConfig;
import app.ServentInfo;
import servent.message.CompareTokenMessage;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.TokenRequestMessage;
import servent.message.util.MessageUtil;

public class LostNodeHandler implements MessageHandler{

    private Message clientMessage;

    public LostNodeHandler(Message clientMessage) {
        this.clientMessage = clientMessage;
    }

    @Override
    public void run() {
        if (clientMessage.getMessageType() == MessageType.LOST) {
            int port;
            String[] splitText = clientMessage.getMessageText().split(":");
            if (splitText.length == 2) {
                try {
                    //The stopped node has alerted us
                    port = Integer.parseInt(splitText[0]);
                    int replacementNode = Integer.parseInt(splitText[1]);
                    if (clientMessage.getSenderPort() == port && AppConfig.chordState.getPredecessor().getListenerPort() == port) {
                        //The stopped node is our predecessor, take over their files and their predecessor
                        AppConfig.timestampedStandardPrint("Switching predecessor " + port);
                        for (ServentInfo si : AppConfig.chordState.getAllNodeInfo()) {
                            if (si.getListenerPort() == replacementNode) {
                                AppConfig.chordState.setPredecessor(si);
                                break;
                            }
                        }
                        AppConfig.chordState.takeOverFilesFromBackup(port);
                    }
                    /*
                    if (AppConfig.chordState.getNeighbourWithToken() == port) {
                        CompareTokenMessage ctm = new CompareTokenMessage(AppConfig.myServentInfo.getListenerPort(), AppConfig.chordState.getPredecessor().getListenerPort(), AppConfig.token.toString());
                        MessageUtil.sendMessage(ctm);
                    }*/
                    AppConfig.chordState.removeNode(port);
                } catch (NumberFormatException e) {
                    AppConfig.timestampedErrorPrint("Got lost node message with bad text: " + clientMessage.getMessageText());
                }
            } else {
                //alert from different node
                try {
                    //check if node is there to be removed
                    int pr = 0;
                    port = Integer.parseInt(clientMessage.getMessageText());
                    for (ServentInfo si : AppConfig.chordState.getAllNodeInfo()) {
                        if (si.getListenerPort() == port) {
                            pr = 1;
                            break;
                        }
                    }
                    if (pr == 1) {
                        if (AppConfig.chordState.getPredecessor().getListenerPort() == port) {
                            //The fallen node was our predecessor, take over their files and their predecessor
                            AppConfig.timestampedStandardPrint("Switching predecessor " + port);
                            for (ServentInfo si : AppConfig.chordState.getAllNodeInfo()) {
                                if (si.getListenerPort() == clientMessage.getSenderPort()) {
                                    AppConfig.chordState.setPredecessor(si);
                                    break;
                                }
                            }/*
                            if (AppConfig.chordState.getNeighbourWithToken() == port) {
                                CompareTokenMessage ctm = new CompareTokenMessage(AppConfig.myServentInfo.getListenerPort(), AppConfig.chordState.getPredecessor().getListenerPort(), AppConfig.token.toString());
                                MessageUtil.sendMessage(ctm);
                            }*/
                            AppConfig.chordState.takeOverFilesFromBackup(port);
                        }
                        AppConfig.chordState.removeNode(port);
                    }
                } catch (NumberFormatException e) {
                    AppConfig.timestampedErrorPrint("Got lost node message with bad text: " + clientMessage.getMessageText());
                }
            }
        } else {
            AppConfig.timestampedErrorPrint("Lost node handler got a message that is not LOST");
        }
    }
}

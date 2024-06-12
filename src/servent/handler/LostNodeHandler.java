package servent.handler;

import app.AppConfig;
import app.ServentInfo;
import servent.message.LostNodeMessage;
import servent.message.Message;
import servent.message.MessageType;
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
            if (splitText.length == 3) {
                try {
                    //The stopped node has alerted us
                    port = Integer.parseInt(splitText[0]);
                    int replacementNode = Integer.parseInt(splitText[1]);
                    String direction = splitText[2];
                    if (clientMessage.getSenderPort() == port && AppConfig.chordState.getPredecessor().getListenerPort() == port && replacementNode != 0) {
                        //The stopped node is our predecessor, take over their files and their predecessor
                        AppConfig.timestampedStandardPrint("Switching predecessor " + port);
                        for (ServentInfo si : AppConfig.chordState.getAllNodeInfo()) {
                            if (si.getListenerPort() == replacementNode) {
                                AppConfig.chordState.setPredecessor(si);
                                break;
                            }
                        }
                    }
                    AppConfig.chordState.removeNode(port);
                    if (direction.equals("F")) {
                        LostNodeMessage lnm = new LostNodeMessage(AppConfig.myServentInfo.getListenerPort(), AppConfig.chordState.getNextNodePort(), String.valueOf(port), "F");
                        MessageUtil.sendMessage(lnm);
                    } else {
                        LostNodeMessage lnm = new LostNodeMessage(AppConfig.myServentInfo.getListenerPort(), AppConfig.chordState.getPredecessor().getListenerPort(), String.valueOf(port), "B");
                        MessageUtil.sendMessage(lnm);
                    }
                    AppConfig.chordState.takeOverFilesFromBackup(port);
                } catch (NumberFormatException e) {
                    AppConfig.timestampedErrorPrint("Got lost node message with bad text: " + clientMessage.getMessageText());
                }
            } else if (splitText.length == 2) {
                //alert from different node
                try {
                    //check if node is there to be removed
                    int pr = 0;
                    port = Integer.parseInt(splitText[0]);
                    String direction = splitText[1];
                    for (ServentInfo si : AppConfig.chordState.getAllNodeInfo()) {
                        if (si.getListenerPort() == port) {
                            pr = 1;
                            break;
                        }
                    }
                    if (pr == 1) {
                        if (AppConfig.chordState.getPredecessor().getListenerPort() == port) {
                            //The fallen node was our predecessor, take over their files, predecessor and token if present
                            AppConfig.timestampedStandardPrint("Switching predecessor " + port);
                            for (ServentInfo si : AppConfig.chordState.getAllNodeInfo()) {
                                if (si.getListenerPort() == clientMessage.getSenderPort()) {
                                    AppConfig.chordState.setPredecessor(si);
                                    break;
                                }
                            }
                            if (AppConfig.chordState.getNeighbourWithToken() == port) {
                                AppConfig.hasToken.set(true);
                                AppConfig.timestampedStandardPrint("Fallen node had token, taking over");
                            }
                            AppConfig.chordState.takeOverFilesFromBackup(port);
                        }
                        AppConfig.chordState.removeNode(port);
                        if (direction.equals("F")) {
                            LostNodeMessage lnm = new LostNodeMessage(clientMessage.getSenderPort(), AppConfig.chordState.getNextNodePort(), String.valueOf(port), "F");
                            MessageUtil.sendMessage(lnm);
                        } else {
                            LostNodeMessage lnm = new LostNodeMessage(clientMessage.getSenderPort(), AppConfig.chordState.getPredecessor().getListenerPort(), String.valueOf(port), "B");
                            MessageUtil.sendMessage(lnm);
                        }
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

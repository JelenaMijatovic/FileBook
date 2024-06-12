package cli.command;

import app.AppConfig;
import app.ServentInfo;
import cli.CLIParser;
import servent.SimpleServentListener;
import servent.message.LostNodeMessage;
import servent.message.TokenSendMessage;
import servent.message.util.MessageUtil;

import java.util.Objects;

public class StopCommand implements CLICommand {

	private CLIParser parser;
	private SimpleServentListener listener;
	
	public StopCommand(CLIParser parser, SimpleServentListener listener) {
		this.parser = parser;
		this.listener = listener;
	}
	
	@Override
	public String commandName() {
		return "stop";
	}

	@Override
	public void execute(String args) {
		AppConfig.timestampedStandardPrint("Commencing shutdown...");
		//Alerting our two direct neighbours
		LostNodeMessage lnm = new LostNodeMessage(AppConfig.myServentInfo.getListenerPort(), AppConfig.chordState.getNextNodePort(), AppConfig.myServentInfo.getListenerPort() + ":" + AppConfig.chordState.getPredecessor().getListenerPort(), "F");
		MessageUtil.sendMessage(lnm);
		lnm = new LostNodeMessage(AppConfig.myServentInfo.getListenerPort(), AppConfig.chordState.getPredecessor().getListenerPort(), AppConfig.myServentInfo.getListenerPort() + ":0", "B");
		MessageUtil.sendMessage(lnm);
		//attempting to pass token to the more responsive neighbour
		if (AppConfig.hasToken.get()) {
			if (Objects.equals(AppConfig.chordState.getBackupSuccessors()[0], AppConfig.chordState.getBackupSuccessors()[1]) || AppConfig.chordState.getBackupLateCount()[0] <= AppConfig.chordState.getBackupLateCount()[1]) {
				TokenSendMessage tsm = new TokenSendMessage(AppConfig.myServentInfo.getListenerPort(), AppConfig.chordState.getPredecessor().getListenerPort(), 9001, AppConfig.token.toString());
				MessageUtil.sendMessage(tsm);
			} else {
				TokenSendMessage tsm = new TokenSendMessage(AppConfig.myServentInfo.getListenerPort(), AppConfig.chordState.getNextNodePort(), 9001, AppConfig.token.toString());
				MessageUtil.sendMessage(tsm);
			}
		}
		AppConfig.chordState.stopTimer();
		listener.stop();
		parser.stop();
		AppConfig.hasToken.set(true);
	}

}

package cli.command;

import app.AppConfig;
import app.ServentInfo;
import cli.CLIParser;
import servent.SimpleServentListener;
import servent.message.LostNodeMessage;
import servent.message.TokenSendMessage;
import servent.message.util.MessageUtil;

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
		AppConfig.chordState.stopTimer();
		//Alerting our two direct neighbours
		LostNodeMessage lnm = new LostNodeMessage(AppConfig.myServentInfo.getListenerPort(), AppConfig.chordState.getNextNodePort(), AppConfig.myServentInfo.getListenerPort() + ":" + AppConfig.chordState.getPredecessor().getListenerPort());
		MessageUtil.sendMessage(lnm);
		lnm = new LostNodeMessage(AppConfig.myServentInfo.getListenerPort(), AppConfig.chordState.getPredecessor().getListenerPort(), String.valueOf(AppConfig.myServentInfo.getListenerPort()));
		MessageUtil.sendMessage(lnm);
		//attempting to pass token to predecessor
		if (AppConfig.hasToken.get()) {
			TokenSendMessage tsm = new TokenSendMessage(AppConfig.myServentInfo.getListenerPort(), AppConfig.chordState.getPredecessor().getListenerPort(), 9001, AppConfig.token.toString());
			MessageUtil.sendMessage(tsm);
		}
		listener.stop();
		parser.stop();
		AppConfig.hasToken.set(true);
	}

}

package cli.command;

import app.AppConfig;
import app.ServentInfo;
import cli.CLIParser;
import servent.SimpleServentListener;

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
		for (ServentInfo si: AppConfig.chordState.getSuccessorTable()) {
			//si.getListenerPort()
		}
		AppConfig.timestampedStandardPrint("Stopping...");
		parser.stop();
		listener.stop();
	}

}

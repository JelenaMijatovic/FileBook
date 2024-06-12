package cli.command;

import app.AppConfig;
import cli.CLIParser;
import servent.SimpleServentListener;

public class SelfDestructCommand implements CLICommand {

    private CLIParser parser;
    private SimpleServentListener listener;

    public SelfDestructCommand(CLIParser parser, SimpleServentListener listener) {
        this.parser = parser;
        this.listener = listener;
    }

    @Override
    public String commandName() {
        return "self_destruct";
    }

    @Override
    public void execute(String args) {
        AppConfig.timestampedStandardPrint("Exploding!");
        AppConfig.chordState.stopTimer();
        listener.stop();
        parser.stop();
        AppConfig.hasToken.set(true);
    }
}

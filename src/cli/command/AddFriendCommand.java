package cli.command;

import app.AppConfig;

public class AddFriendCommand implements CLICommand {
    @Override
    public String commandName() {
        return "add_friend";
    }

    @Override
    public void execute(String args) {
        String[] splitText = args.split(":");
        Integer port = Integer.valueOf(splitText[1]);
        if (AppConfig.chordState.getFriends().contains(port) || AppConfig.myServentInfo.getChordId() == port) {
            AppConfig.timestampedErrorPrint("add_friend: You are already friends");
            return;
        }
        AppConfig.chordState.getFriends().add(port);
        AppConfig.timestampedStandardPrint(port + " added as friend");
    }
}

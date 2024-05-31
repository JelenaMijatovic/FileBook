package cli.command;

import app.AppConfig;

public class ViewFilesCommand implements CLICommand{

    @Override
    public String commandName() {
        return "view_files";
    }

    @Override
    public void execute(String args) {
        if (args != null) {
            String[] splitArgs = args.split(":");
            if (splitArgs.length == 2) {
                String address = splitArgs[0];
                int port = Integer.parseInt(splitArgs[1]);
                if (AppConfig.myServentInfo.getIpAddress().equals(address) && AppConfig.myServentInfo.getListenerPort() == port) {
                    AppConfig.timestampedStandardPrint(AppConfig.chordState.listFiles(port));
                    return;
                }
                AppConfig.chordState.getFiles(AppConfig.myServentInfo.getListenerPort(), port);
            } else {
                AppConfig.timestampedErrorPrint("Incorrect number of arguments for add_file");
            }
        } else {
            AppConfig.timestampedStandardPrint(AppConfig.chordState.listFiles(AppConfig.myServentInfo.getListenerPort()));
        }
    }
}

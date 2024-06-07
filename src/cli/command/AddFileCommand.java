package cli.command;

import app.AppConfig;

import java.util.Random;

import static java.lang.Math.abs;

public class AddFileCommand implements CLICommand {

    @Override
    public String commandName() {
        return "add_file";
    }

    @Override
    public void execute(String args) {
        String[] splitArgs = args.split(" ");
        if (splitArgs.length == 2) {
            String path = splitArgs[0];
            String visibility = splitArgs[1];
            int key = abs(path.hashCode()) % 64;
            if (visibility.equals("public")) {
                AppConfig.chordState.askAddFile(key, path, 0);
            }
            else if (visibility.equals("private")) {
                AppConfig.chordState.askAddFile(key, path, 1);
            }
            else
                AppConfig.timestampedErrorPrint("add_file: Incorrect visiblity argument");
        } else {
            AppConfig.timestampedErrorPrint("Incorrect number of arguments for add_file");
        }
    }
}

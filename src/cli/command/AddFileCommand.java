package cli.command;

import app.AppConfig;

import java.nio.file.Files;
import java.nio.file.Path;

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
            if (Files.exists(Path.of(AppConfig.root + "/" + path))) {
                AppConfig.timestampedErrorPrint("add_file: File with filename " + path + " already present on servent");
                return;
            }
            if (visibility.equals("public"))
                AppConfig.chordState.addFile(path, 0);
            else if (visibility.equals("private"))
                AppConfig.chordState.addFile(path, 1);
            else
                AppConfig.timestampedErrorPrint("add_file: Incorrect visiblity argument");
        } else {
            AppConfig.timestampedErrorPrint("Incorrect number of arguments for add_file");
        }
    }
}

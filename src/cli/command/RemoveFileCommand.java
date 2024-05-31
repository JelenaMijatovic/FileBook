package cli.command;

import app.AppConfig;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

public class RemoveFileCommand implements CLICommand {

    @Override
    public String commandName() {
        return "remove_file";
    }

    @Override
    public void execute(String args) {
        String path = args;
        if (!Files.exists(Path.of(AppConfig.root + "/" + path))) {
            AppConfig.timestampedErrorPrint("remove_file: file " + path + " doesn't exist");
        } else {
            AppConfig.chordState.removeFile(path);
        }
    }
}

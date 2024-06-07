package cli.command;

import app.AppConfig;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.lang.Math.abs;

public class RemoveFileCommand implements CLICommand {

    @Override
    public String commandName() {
        return "remove_file";
    }

    @Override
    public void execute(String args) {
        String path = args;
        AppConfig.chordState.askRemoveFile(abs(path.hashCode()) % 64, path);
    }
}

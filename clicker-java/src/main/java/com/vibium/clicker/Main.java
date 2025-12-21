package com.vibium.clicker;

import com.vibium.clicker.cli.ClickerCommand;
import picocli.CommandLine;

public class Main {
    public static void main(String[] args) {
        int exitCode = new CommandLine(new ClickerCommand()).execute(args);
        System.exit(exitCode);
    }
}

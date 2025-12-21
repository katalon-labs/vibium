package com.vibium.clicker.cli;

import picocli.CommandLine.Command;

@Command(
    name = "version",
    description = "Print version information"
)
public class VersionCommand implements Runnable {

    public static final String VERSION = "0.1.0";

    @Override
    public void run() {
        System.out.println("Clicker Java v" + VERSION);
        System.out.println("WebDriver BiDi browser automation");
    }
}

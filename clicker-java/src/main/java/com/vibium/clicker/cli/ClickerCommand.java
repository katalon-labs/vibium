package com.vibium.clicker.cli;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
    name = "clicker-java",
    description = "Browser automation CLI - Java implementation",
    mixinStandardHelpOptions = true,
    version = "Clicker Java v0.1.0",
    subcommands = {
        VersionCommand.class,
        PathsCommand.class,
        InstallCommand.class,
        LaunchTestCommand.class,
        WsTestCommand.class,
        BidiTestCommand.class,
        NavigateCommand.class,
        ScreenshotCommand.class,
        EvalCommand.class,
        FindCommand.class,
        ClickCommand.class,
        TypeCommand.class,
        ServeCommand.class,
        CommandLine.HelpCommand.class
    }
)
public class ClickerCommand implements Runnable {

    @Option(names = "--headed", description = "Run browser in headed mode (visible window)")
    public static boolean headed = false;

    @Option(names = "--wait-open", description = "Seconds to wait after navigation")
    public static int waitOpen = 0;

    @Option(names = "--wait-close", description = "Seconds to wait before closing browser")
    public static int waitClose = 0;

    @Option(names = "--verbose", description = "Enable verbose output")
    public static boolean verbose = false;

    @Override
    public void run() {
        CommandLine.usage(this, System.out);
    }

    public static boolean isHeadless() {
        return !headed;
    }
}

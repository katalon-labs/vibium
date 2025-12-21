package com.vibium.clicker.cli;

import com.vibium.clicker.browser.BrowserLauncher;
import com.vibium.clicker.browser.LaunchResult;
import picocli.CommandLine.Command;

@Command(
    name = "launch-test",
    description = "Launch browser and print WebSocket URL"
)
public class LaunchTestCommand implements Runnable {

    @Override
    public void run() {
        BrowserLauncher launcher = new BrowserLauncher(ClickerCommand.verbose);
        LaunchResult result = null;

        try {
            System.out.println("Launching browser...");
            result = launcher.launch(ClickerCommand.isHeadless());

            System.out.println("WebSocket URL: " + result.wsUrl());
            System.out.println("Port: " + result.port());

            if (ClickerCommand.waitClose > 0) {
                System.out.println("Waiting " + ClickerCommand.waitClose + " seconds...");
                Thread.sleep(ClickerCommand.waitClose * 1000L);
            } else if (ClickerCommand.headed) {
                System.out.println("Press Enter to close browser...");
                System.in.read();
            }
        } catch (Exception e) {
            System.err.println("Launch failed: " + e.getMessage());
            if (ClickerCommand.verbose) {
                e.printStackTrace();
            }
            System.exit(1);
        } finally {
            if (result != null) {
                launcher.close(result);
            }
        }
    }
}

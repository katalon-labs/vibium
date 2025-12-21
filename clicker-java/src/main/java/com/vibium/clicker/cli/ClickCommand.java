package com.vibium.clicker.cli;

import com.vibium.clicker.bidi.BiDiClient;
import com.vibium.clicker.bidi.BiDiConnection;
import com.vibium.clicker.browser.BrowserLauncher;
import com.vibium.clicker.browser.LaunchResult;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(
    name = "click",
    description = "Navigate to URL and click element"
)
public class ClickCommand implements Runnable {

    @Parameters(index = "0", description = "URL to navigate to")
    private String url;

    @Parameters(index = "1", description = "CSS selector of element to click")
    private String selector;

    @Override
    public void run() {
        BrowserLauncher launcher = new BrowserLauncher(ClickerCommand.verbose);
        LaunchResult result = null;

        try {
            result = launcher.launch(ClickerCommand.isHeadless());
            BiDiConnection conn = BiDiConnection.connect(result.wsUrl());
            BiDiClient client = new BiDiClient(conn);
            client.setVerbose(ClickerCommand.verbose);

            System.out.println("Navigating to: " + url);
            client.navigate("", url);

            if (ClickerCommand.waitOpen > 0) {
                Thread.sleep(ClickerCommand.waitOpen * 1000L);
            }

            System.out.println("Clicking element: " + selector);
            client.clickElement("", selector);

            // Wait for navigation/action
            Thread.sleep(500);

            // Get current URL after click
            String currentUrl = client.getCurrentUrl("");

            System.out.println();
            System.out.println("Click complete!");
            System.out.println("Current URL: " + currentUrl);

            if (ClickerCommand.waitClose > 0) {
                System.out.println("Waiting " + ClickerCommand.waitClose + " seconds...");
                Thread.sleep(ClickerCommand.waitClose * 1000L);
            } else if (ClickerCommand.headed) {
                System.out.println("\nPress Enter to close browser...");
                System.in.read();
            }

            client.close();
        } catch (Exception e) {
            System.err.println("Click failed: " + e.getMessage());
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

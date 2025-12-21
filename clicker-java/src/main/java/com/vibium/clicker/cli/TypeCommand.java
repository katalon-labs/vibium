package com.vibium.clicker.cli;

import com.vibium.clicker.bidi.BiDiClient;
import com.vibium.clicker.bidi.BiDiConnection;
import com.vibium.clicker.browser.BrowserLauncher;
import com.vibium.clicker.browser.LaunchResult;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(
    name = "type",
    description = "Navigate to URL, click element, and type text"
)
public class TypeCommand implements Runnable {

    @Parameters(index = "0", description = "URL to navigate to")
    private String url;

    @Parameters(index = "1", description = "CSS selector of input element")
    private String selector;

    @Parameters(index = "2", description = "Text to type")
    private String text;

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

            System.out.println("Typing into element: " + selector);
            client.typeIntoElement("", selector, text);

            // Get value after typing
            Thread.sleep(100);
            String value = client.getElementValue("", selector);

            System.out.println();
            System.out.println("Typed \"" + text + "\"");
            System.out.println("Current value: " + value);

            if (ClickerCommand.waitClose > 0) {
                System.out.println("Waiting " + ClickerCommand.waitClose + " seconds...");
                Thread.sleep(ClickerCommand.waitClose * 1000L);
            } else if (ClickerCommand.headed) {
                System.out.println("\nPress Enter to close browser...");
                System.in.read();
            }

            client.close();
        } catch (Exception e) {
            System.err.println("Type failed: " + e.getMessage());
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

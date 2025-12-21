package com.vibium.clicker.cli;

import com.vibium.clicker.bidi.BiDiClient;
import com.vibium.clicker.bidi.BiDiConnection;
import com.vibium.clicker.bidi.ElementInfo;
import com.vibium.clicker.browser.BrowserLauncher;
import com.vibium.clicker.browser.LaunchResult;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(
    name = "find",
    description = "Navigate to URL and find element by CSS selector"
)
public class FindCommand implements Runnable {

    @Parameters(index = "0", description = "URL to navigate to")
    private String url;

    @Parameters(index = "1", description = "CSS selector")
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

            System.out.println("Finding element: " + selector);
            ElementInfo element = client.findElement("", selector);

            System.out.println();
            if (element != null) {
                System.out.println("Element found:");
                System.out.println("  tag: " + element.tagName);
                System.out.println("  text: \"" + element.textContent + "\"");
                System.out.printf("  box: {x: %.0f, y: %.0f, width: %.0f, height: %.0f}%n",
                    element.x, element.y, element.width, element.height);
                System.out.printf("  center: (%.0f, %.0f)%n", element.getCenterX(), element.getCenterY());
            } else {
                System.out.println("Element not found: " + selector);
            }

            if (ClickerCommand.waitClose > 0) {
                Thread.sleep(ClickerCommand.waitClose * 1000L);
            } else if (ClickerCommand.headed) {
                System.out.println("\nPress Enter to close browser...");
                System.in.read();
            }

            client.close();
        } catch (Exception e) {
            System.err.println("Find failed: " + e.getMessage());
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

package com.vibium.clicker.cli;

import com.vibium.clicker.bidi.BiDiClient;
import com.vibium.clicker.bidi.BiDiConnection;
import com.vibium.clicker.bidi.BiDiProtocol;
import com.vibium.clicker.browser.BrowserLauncher;
import com.vibium.clicker.browser.LaunchResult;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(
    name = "navigate",
    description = "Navigate to URL and print page info"
)
public class NavigateCommand implements Runnable {

    @Parameters(index = "0", description = "URL to navigate to")
    private String url;

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
            BiDiProtocol.NavigateResult navResult = client.navigate("", url);

            if (ClickerCommand.waitOpen > 0) {
                Thread.sleep(ClickerCommand.waitOpen * 1000L);
            }

            // Get title
            BiDiProtocol.EvaluateResult evalResult = client.evaluate("", "document.title");
            String title = "";
            if (evalResult.result != null && evalResult.result.value != null) {
                title = evalResult.result.value.toString();
            }

            System.out.println();
            System.out.println("Navigation complete:");
            System.out.println("  URL: " + navResult.url);
            System.out.println("  Title: " + title);

            if (ClickerCommand.waitClose > 0) {
                System.out.println("Waiting " + ClickerCommand.waitClose + " seconds...");
                Thread.sleep(ClickerCommand.waitClose * 1000L);
            } else if (ClickerCommand.headed) {
                System.out.println("Press Enter to close browser...");
                System.in.read();
            }

            client.close();
        } catch (Exception e) {
            System.err.println("Navigation failed: " + e.getMessage());
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

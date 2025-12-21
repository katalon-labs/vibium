package com.vibium.clicker.cli;

import com.vibium.clicker.bidi.BiDiClient;
import com.vibium.clicker.bidi.BiDiConnection;
import com.vibium.clicker.bidi.BiDiProtocol;
import com.vibium.clicker.browser.BrowserLauncher;
import com.vibium.clicker.browser.LaunchResult;
import picocli.CommandLine.Command;

@Command(
    name = "bidi-test",
    description = "Launch browser and test BiDi session.status"
)
public class BidiTestCommand implements Runnable {

    @Override
    public void run() {
        BrowserLauncher launcher = new BrowserLauncher(ClickerCommand.verbose);
        LaunchResult result = null;

        try {
            System.out.println("Launching browser...");
            result = launcher.launch(ClickerCommand.isHeadless());

            System.out.println("Connecting to BiDi WebSocket...");
            BiDiConnection conn = BiDiConnection.connect(result.wsUrl());
            BiDiClient client = new BiDiClient(conn);
            client.setVerbose(ClickerCommand.verbose);

            System.out.println("Sending session.status...");
            BiDiProtocol.SessionStatus status = client.sessionStatus();

            System.out.println();
            System.out.println("Session Status:");
            System.out.println("  ready: " + status.ready);
            System.out.println("  message: " + status.message);

            client.close();
        } catch (Exception e) {
            System.err.println("BiDi test failed: " + e.getMessage());
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

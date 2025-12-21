package com.vibium.clicker.cli;

import com.vibium.clicker.bidi.BiDiClient;
import com.vibium.clicker.bidi.BiDiConnection;
import com.vibium.clicker.bidi.BiDiProtocol;
import com.vibium.clicker.browser.BrowserLauncher;
import com.vibium.clicker.browser.LaunchResult;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(
    name = "eval",
    description = "Navigate to URL and evaluate JavaScript expression"
)
public class EvalCommand implements Runnable {

    @Parameters(index = "0", description = "URL to navigate to")
    private String url;

    @Parameters(index = "1", description = "JavaScript expression to evaluate")
    private String expression;

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

            System.out.println("Evaluating: " + expression);
            BiDiProtocol.EvaluateResult evalResult = client.evaluate("", expression);

            System.out.println();
            if (evalResult.exceptionDetails != null) {
                System.out.println("Error: " + evalResult.exceptionDetails.text);
            } else if (evalResult.result != null) {
                System.out.println("Result type: " + evalResult.result.type);
                System.out.println("Result value: " + evalResult.result.value);
            }

            if (ClickerCommand.waitClose > 0) {
                Thread.sleep(ClickerCommand.waitClose * 1000L);
            } else if (ClickerCommand.headed) {
                System.out.println("\nPress Enter to close browser...");
                System.in.read();
            }

            client.close();
        } catch (Exception e) {
            System.err.println("Eval failed: " + e.getMessage());
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

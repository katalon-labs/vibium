package com.vibium.clicker.cli;

import com.vibium.clicker.bidi.BiDiClient;
import com.vibium.clicker.bidi.BiDiConnection;
import com.vibium.clicker.browser.BrowserLauncher;
import com.vibium.clicker.browser.LaunchResult;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.nio.file.Files;
import java.nio.file.Path;

@Command(
    name = "screenshot",
    description = "Navigate to URL and capture screenshot"
)
public class ScreenshotCommand implements Runnable {

    @Parameters(index = "0", description = "URL to navigate to")
    private String url;

    @Option(names = {"-o", "--output"}, description = "Output file path", defaultValue = "screenshot.png")
    private String output;

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

            System.out.println("Capturing screenshot...");
            byte[] screenshot = client.captureScreenshot("");

            Path outputPath = Path.of(output);
            Files.write(outputPath, screenshot);

            System.out.println("Screenshot saved to: " + outputPath.toAbsolutePath());
            System.out.println("Size: " + screenshot.length + " bytes");

            client.close();
        } catch (Exception e) {
            System.err.println("Screenshot failed: " + e.getMessage());
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

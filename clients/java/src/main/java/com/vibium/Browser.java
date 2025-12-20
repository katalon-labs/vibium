package com.vibium;

import com.vibium.bidi.BiDiClient;
import com.vibium.clicker.BinaryResolver;
import com.vibium.clicker.ClickerProcess;

/**
 * Entry point for launching browser automation.
 * Mirrors clients/javascript/src/browser.ts
 */
public final class Browser {

    private Browser() {
        // Utility class
    }

    /**
     * Launch a browser with default options (headless).
     *
     * @return A Vibe instance for browser automation
     */
    public static Vibe launch() {
        return launch(new LaunchOptions());
    }

    /**
     * Launch a browser with the specified options.
     *
     * @param options Launch configuration options
     * @return A Vibe instance for browser automation
     */
    public static Vibe launch(LaunchOptions options) {
        // Resolve the binary path
        String binaryPath = BinaryResolver.resolve(options.getExecutablePath());

        // Start the clicker process
        ClickerProcess process = ClickerProcess.start(
            binaryPath,
            options.getPort(),
            options.isHeadless()
        );

        // Connect to the proxy
        BiDiClient client = BiDiClient.connect("ws://localhost:" + process.getPort());

        return new Vibe(client, process);
    }
}

package com.vibium.clicker.cli;

import com.vibium.clicker.browser.BrowserInstaller;
import com.vibium.clicker.paths.PlatformPaths;
import picocli.CommandLine.Command;

import java.nio.file.Path;

@Command(
    name = "install",
    description = "Download Chrome for Testing and chromedriver"
)
public class InstallCommand implements Runnable {

    @Override
    public void run() {
        // Check for skip environment variable
        String skipDownload = System.getenv("VIBIUM_SKIP_BROWSER_DOWNLOAD");
        if ("1".equals(skipDownload) || "true".equalsIgnoreCase(skipDownload)) {
            System.out.println("VIBIUM_SKIP_BROWSER_DOWNLOAD is set, skipping browser download");
            return;
        }

        System.out.println("Installing Chrome for Testing...");
        System.out.println("Platform: " + PlatformPaths.getPlatformString());
        System.out.println();

        try {
            BrowserInstaller installer = new BrowserInstaller();
            Path installPath = installer.install();

            System.out.println();
            System.out.println("Installation complete!");
            System.out.println("Installed to: " + installPath);
            System.out.println();

            // Show paths
            Path chrome = PlatformPaths.getChromeExecutable();
            Path chromedriver = PlatformPaths.getChromedriverExecutable();

            if (chrome != null) {
                System.out.println("Chrome: " + chrome);
            }
            if (chromedriver != null) {
                System.out.println("Chromedriver: " + chromedriver);
            }
        } catch (Exception e) {
            System.err.println("Installation failed: " + e.getMessage());
            if (ClickerCommand.verbose) {
                e.printStackTrace();
            }
            System.exit(1);
        }
    }
}

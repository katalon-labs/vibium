package com.vibium.clicker.cli;

import com.vibium.clicker.paths.PlatformPaths;
import picocli.CommandLine.Command;

import java.nio.file.Files;
import java.nio.file.Path;

@Command(
    name = "paths",
    description = "Show platform-specific paths"
)
public class PathsCommand implements Runnable {

    @Override
    public void run() {
        System.out.println("Platform: " + PlatformPaths.getOS() + "/" + PlatformPaths.getArch());
        System.out.println();

        Path cacheDir = PlatformPaths.getCacheDir();
        System.out.println("Cache directory: " + cacheDir);
        System.out.println("  exists: " + Files.exists(cacheDir));
        System.out.println();

        Path chromePath = PlatformPaths.getChromeExecutable();
        if (chromePath != null) {
            System.out.println("Chrome: " + chromePath);
            System.out.println("  exists: " + Files.exists(chromePath));
        } else {
            System.out.println("Chrome: not found");
        }
        System.out.println();

        Path chromedriverPath = PlatformPaths.getChromedriverExecutable();
        if (chromedriverPath != null) {
            System.out.println("Chromedriver: " + chromedriverPath);
            System.out.println("  exists: " + Files.exists(chromedriverPath));
        } else {
            System.out.println("Chromedriver: not found");
        }
    }
}

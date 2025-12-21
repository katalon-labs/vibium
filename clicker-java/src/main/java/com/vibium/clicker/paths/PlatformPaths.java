package com.vibium.clicker.paths;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.stream.Stream;

public class PlatformPaths {

    private static final String CACHE_SUBDIR = "vibium";
    private static final String CHROME_SUBDIR = "chrome-for-testing";

    public static String getOS() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("linux")) return "linux";
        if (os.contains("mac") || os.contains("darwin")) return "darwin";
        if (os.contains("win")) return "windows";
        return "unknown";
    }

    public static String getArch() {
        String arch = System.getProperty("os.arch").toLowerCase();
        if (arch.contains("aarch64") || arch.contains("arm64")) return "arm64";
        if (arch.contains("amd64") || arch.contains("x86_64")) return "x64";
        return arch;
    }

    public static String getPlatformString() {
        String os = getOS();
        String arch = getArch();

        return switch (os) {
            case "linux" -> arch.equals("arm64") ? "linux-arm64" : "linux64";
            case "darwin" -> arch.equals("arm64") ? "mac-arm64" : "mac-x64";
            case "windows" -> "win64";
            default -> "unknown";
        };
    }

    public static Path getCacheDir() {
        String os = getOS();
        Path cacheBase;

        switch (os) {
            case "linux" -> {
                String xdgCache = System.getenv("XDG_CACHE_HOME");
                if (xdgCache != null && !xdgCache.isEmpty()) {
                    cacheBase = Paths.get(xdgCache);
                } else {
                    cacheBase = Paths.get(System.getProperty("user.home"), ".cache");
                }
            }
            case "darwin" -> cacheBase = Paths.get(System.getProperty("user.home"), "Library", "Caches");
            case "windows" -> {
                String localAppData = System.getenv("LOCALAPPDATA");
                if (localAppData != null && !localAppData.isEmpty()) {
                    cacheBase = Paths.get(localAppData);
                } else {
                    cacheBase = Paths.get(System.getProperty("user.home"), "AppData", "Local");
                }
            }
            default -> cacheBase = Paths.get(System.getProperty("user.home"), ".cache");
        }

        return cacheBase.resolve(CACHE_SUBDIR);
    }

    public static Path getChromeForTestingDir() {
        return getCacheDir().resolve(CHROME_SUBDIR);
    }

    public static Path getLatestChromeVersion() {
        Path cftDir = getChromeForTestingDir();
        if (!Files.exists(cftDir)) {
            return null;
        }

        try (Stream<Path> dirs = Files.list(cftDir)) {
            return dirs
                .filter(Files::isDirectory)
                .filter(p -> p.getFileName().toString().matches("\\d+\\..*"))
                .max(Comparator.comparing(p -> p.getFileName().toString()))
                .orElse(null);
        } catch (IOException e) {
            return null;
        }
    }

    public static Path getChromeExecutable() {
        // First check cached Chrome for Testing
        Path versionDir = getLatestChromeVersion();
        if (versionDir != null) {
            Path cachedChrome = getCachedChromeExecutable(versionDir);
            if (cachedChrome != null && Files.exists(cachedChrome)) {
                return cachedChrome;
            }
        }

        // Fall back to system Chrome
        return getSystemChromeExecutable();
    }

    private static Path getCachedChromeExecutable(Path versionDir) {
        String os = getOS();

        return switch (os) {
            case "linux" -> versionDir.resolve("chrome-linux64").resolve("chrome");
            case "darwin" -> {
                String arch = getArch();
                String subdir = arch.equals("arm64") ? "chrome-mac-arm64" : "chrome-mac-x64";
                yield versionDir.resolve(subdir)
                    .resolve("Google Chrome for Testing.app")
                    .resolve("Contents")
                    .resolve("MacOS")
                    .resolve("Google Chrome for Testing");
            }
            case "windows" -> versionDir.resolve("chrome-win64").resolve("chrome.exe");
            default -> null;
        };
    }

    private static Path getSystemChromeExecutable() {
        String os = getOS();

        switch (os) {
            case "linux" -> {
                String[] linuxPaths = {
                    "/usr/bin/google-chrome",
                    "/usr/bin/google-chrome-stable",
                    "/usr/bin/chromium",
                    "/usr/bin/chromium-browser",
                    "/snap/bin/chromium"
                };
                for (String p : linuxPaths) {
                    Path path = Paths.get(p);
                    if (Files.exists(path)) return path;
                }
            }
            case "darwin" -> {
                String[] macPaths = {
                    "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome",
                    "/Applications/Chromium.app/Contents/MacOS/Chromium"
                };
                for (String p : macPaths) {
                    Path path = Paths.get(p);
                    if (Files.exists(path)) return path;
                }
            }
            case "windows" -> {
                String[] winPaths = {
                    System.getenv("PROGRAMFILES") + "\\Google\\Chrome\\Application\\chrome.exe",
                    System.getenv("PROGRAMFILES(X86)") + "\\Google\\Chrome\\Application\\chrome.exe",
                    System.getenv("LOCALAPPDATA") + "\\Google\\Chrome\\Application\\chrome.exe"
                };
                for (String p : winPaths) {
                    if (p != null) {
                        Path path = Paths.get(p);
                        if (Files.exists(path)) return path;
                    }
                }
            }
        }

        return null;
    }

    public static Path getChromedriverExecutable() {
        Path versionDir = getLatestChromeVersion();
        if (versionDir == null) {
            return null;
        }

        String os = getOS();
        String filename = os.equals("windows") ? "chromedriver.exe" : "chromedriver";

        String subdir = switch (os) {
            case "linux" -> "chromedriver-linux64";
            case "darwin" -> getArch().equals("arm64") ? "chromedriver-mac-arm64" : "chromedriver-mac-x64";
            case "windows" -> "chromedriver-win64";
            default -> null;
        };

        if (subdir == null) return null;

        Path chromedriverPath = versionDir.resolve(subdir).resolve(filename);
        return Files.exists(chromedriverPath) ? chromedriverPath : null;
    }
}

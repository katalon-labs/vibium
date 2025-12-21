package com.vibium.clicker.browser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vibium.clicker.paths.PlatformPaths;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermission;
import java.time.Duration;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class BrowserInstaller {

    private static final String VERSIONS_URL =
        "https://googlechromelabs.github.io/chrome-for-testing/last-known-good-versions-with-downloads.json";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public BrowserInstaller() {
        this.httpClient = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(30))
            .build();
        this.objectMapper = new ObjectMapper();
    }

    public Path install() throws IOException, InterruptedException {
        System.out.println("Fetching version information...");

        // Get version info
        JsonNode versionInfo = fetchVersionInfo();
        JsonNode stable = versionInfo.get("channels").get("Stable");
        String version = stable.get("version").asText();

        System.out.println("Latest stable version: " + version);

        Path versionDir = PlatformPaths.getChromeForTestingDir().resolve(version);

        // Check if already installed
        Path chromePath = PlatformPaths.getChromeExecutable();
        Path chromedriverPath = PlatformPaths.getChromedriverExecutable();

        if (chromePath != null && Files.exists(chromePath) &&
            chromedriverPath != null && Files.exists(chromedriverPath)) {
            System.out.println("Chrome for Testing already installed at: " + versionDir);
            return versionDir;
        }

        // Create version directory
        Files.createDirectories(versionDir);

        String platform = PlatformPaths.getPlatformString();

        // Download Chrome
        JsonNode chromeDownloads = stable.get("downloads").get("chrome");
        String chromeUrl = findDownloadUrl(chromeDownloads, platform);
        if (chromeUrl != null) {
            System.out.println("Downloading Chrome...");
            downloadAndExtract(chromeUrl, versionDir);
        } else {
            System.err.println("No Chrome download available for platform: " + platform);
        }

        // Download Chromedriver
        JsonNode chromedriverDownloads = stable.get("downloads").get("chromedriver");
        String chromedriverUrl = findDownloadUrl(chromedriverDownloads, platform);
        if (chromedriverUrl != null) {
            System.out.println("Downloading Chromedriver...");
            downloadAndExtract(chromedriverUrl, versionDir);
        } else {
            System.err.println("No Chromedriver download available for platform: " + platform);
        }

        // Make executables on Unix
        if (!PlatformPaths.getOS().equals("windows")) {
            makeExecutable(versionDir);
        }

        return versionDir;
    }

    private JsonNode fetchVersionInfo() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(VERSIONS_URL))
            .timeout(Duration.ofSeconds(30))
            .GET()
            .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("Failed to fetch version info: HTTP " + response.statusCode());
        }

        return objectMapper.readTree(response.body());
    }

    private String findDownloadUrl(JsonNode downloads, String platform) {
        if (downloads == null || !downloads.isArray()) {
            return null;
        }

        for (JsonNode download : downloads) {
            if (platform.equals(download.get("platform").asText())) {
                return download.get("url").asText();
            }
        }
        return null;
    }

    private void downloadAndExtract(String url, Path targetDir) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .timeout(Duration.ofMinutes(5))
            .GET()
            .build();

        HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());

        if (response.statusCode() != 200) {
            throw new IOException("Failed to download: HTTP " + response.statusCode());
        }

        // Extract ZIP
        try (ZipInputStream zis = new ZipInputStream(response.body())) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                Path entryPath = targetDir.resolve(entry.getName());

                // Security check: prevent zip slip
                if (!entryPath.normalize().startsWith(targetDir.normalize())) {
                    throw new IOException("Bad zip entry: " + entry.getName());
                }

                if (entry.isDirectory()) {
                    Files.createDirectories(entryPath);
                } else {
                    Files.createDirectories(entryPath.getParent());
                    Files.copy(zis, entryPath, StandardCopyOption.REPLACE_EXISTING);
                }
                zis.closeEntry();
            }
        }
    }

    private void makeExecutable(Path versionDir) throws IOException {
        Set<PosixFilePermission> execPerms = Set.of(
            PosixFilePermission.OWNER_READ,
            PosixFilePermission.OWNER_WRITE,
            PosixFilePermission.OWNER_EXECUTE,
            PosixFilePermission.GROUP_READ,
            PosixFilePermission.GROUP_EXECUTE,
            PosixFilePermission.OTHERS_READ,
            PosixFilePermission.OTHERS_EXECUTE
        );

        // Find and chmod executables
        Files.walk(versionDir)
            .filter(Files::isRegularFile)
            .filter(p -> {
                String name = p.getFileName().toString();
                return name.equals("chrome") ||
                       name.equals("chromedriver") ||
                       name.equals("Google Chrome for Testing") ||
                       name.endsWith(".sh");
            })
            .forEach(p -> {
                try {
                    Files.setPosixFilePermissions(p, execPerms);
                } catch (IOException e) {
                    System.err.println("Warning: Could not set executable permission on: " + p);
                }
            });
    }
}

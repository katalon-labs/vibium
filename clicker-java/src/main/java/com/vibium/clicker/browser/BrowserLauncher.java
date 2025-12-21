package com.vibium.clicker.browser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vibium.clicker.paths.PlatformPaths;
import com.vibium.clicker.process.ProcessManager;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BrowserLauncher {

    private static final List<String> CHROME_FLAGS = List.of(
        "--no-first-run",
        "--no-default-browser-check",
        "--disable-background-networking",
        "--disable-background-timer-throttling",
        "--disable-backgrounding-occluded-windows",
        "--disable-breakpad",
        "--disable-component-extensions-with-background-pages",
        "--disable-component-update",
        "--disable-default-apps",
        "--disable-dev-shm-usage",
        "--disable-extensions",
        "--disable-features=TranslateUI",
        "--disable-hang-monitor",
        "--disable-ipc-flooding-protection",
        "--disable-popup-blocking",
        "--disable-prompt-on-repost",
        "--disable-renderer-backgrounding",
        "--disable-sync",
        "--enable-features=NetworkService,NetworkServiceInProcess",
        "--force-color-profile=srgb",
        "--metrics-recording-only",
        "--password-store=basic",
        "--use-mock-keychain",
        "--disable-blink-features=AutomationControlled"
    );

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final boolean verbose;

    public BrowserLauncher() {
        this(false);
    }

    public BrowserLauncher(boolean verbose) {
        this.verbose = verbose;
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
        this.objectMapper = new ObjectMapper();
    }

    public LaunchResult launch(boolean headless) throws Exception {
        Path chromedriverPath = PlatformPaths.getChromedriverExecutable();
        Path chromePath = PlatformPaths.getChromeExecutable();

        if (chromedriverPath == null || !Files.exists(chromedriverPath)) {
            throw new Exception("Chromedriver not found. Run 'clicker-java install' first.");
        }
        if (chromePath == null || !Files.exists(chromePath)) {
            throw new Exception("Chrome not found. Run 'clicker-java install' first.");
        }

        int port = findAvailablePort();

        if (verbose) {
            System.err.println("[Launcher] Starting chromedriver on port " + port);
            System.err.println("[Launcher] Chrome: " + chromePath);
            System.err.println("[Launcher] Chromedriver: " + chromedriverPath);
        }

        // Start chromedriver
        ProcessBuilder pb = new ProcessBuilder(
            chromedriverPath.toString(),
            "--port=" + port,
            "--silent"
        );
        pb.redirectErrorStream(true);

        Process chromedriverProcess = pb.start();
        ProcessManager.register(chromedriverProcess);

        // Wait for chromedriver to be ready
        waitForChromedriver(port);

        // Create session with BiDi capability
        String wsUrl = createSession(port, chromePath.toString(), headless);

        if (verbose) {
            System.err.println("[Launcher] WebSocket URL: " + wsUrl);
        }

        return new LaunchResult(wsUrl, chromedriverProcess, port);
    }

    private int findAvailablePort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }

    private void waitForChromedriver(int port) throws Exception {
        String statusUrl = "http://localhost:" + port + "/status";
        int maxAttempts = 50;

        for (int i = 0; i < maxAttempts; i++) {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(statusUrl))
                    .timeout(Duration.ofSeconds(1))
                    .GET()
                    .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200) {
                    JsonNode json = objectMapper.readTree(response.body());
                    if (json.has("value") && json.get("value").has("ready") &&
                        json.get("value").get("ready").asBoolean()) {
                        return;
                    }
                }
            } catch (Exception e) {
                // Ignore and retry
            }
            Thread.sleep(100);
        }

        throw new Exception("Chromedriver did not start within timeout");
    }

    private String createSession(int port, String chromePath, boolean headless) throws Exception {
        String sessionUrl = "http://localhost:" + port + "/session";

        // Build Chrome arguments
        List<String> chromeArgs = new ArrayList<>(CHROME_FLAGS);
        if (headless) {
            chromeArgs.add("--headless=new");
        }

        // Build capabilities
        Map<String, Object> chromeOptions = new HashMap<>();
        chromeOptions.put("binary", chromePath);
        chromeOptions.put("args", chromeArgs);

        Map<String, Object> capabilities = new HashMap<>();
        capabilities.put("alwaysMatch", Map.of(
            "goog:chromeOptions", chromeOptions,
            "webSocketUrl", true
        ));

        Map<String, Object> body = Map.of("capabilities", capabilities);
        String jsonBody = objectMapper.writeValueAsString(body);

        if (verbose) {
            System.err.println("[Launcher] Creating session...");
        }

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(sessionUrl))
            .header("Content-Type", "application/json")
            .timeout(Duration.ofSeconds(30))
            .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
            .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new Exception("Failed to create session: HTTP " + response.statusCode() + " - " + response.body());
        }

        JsonNode json = objectMapper.readTree(response.body());
        JsonNode value = json.get("value");

        if (value == null) {
            throw new Exception("Invalid session response: " + response.body());
        }

        // Extract WebSocket URL
        JsonNode wsUrlNode = value.get("capabilities").get("webSocketUrl");
        if (wsUrlNode == null) {
            throw new Exception("WebSocket URL not found in session response");
        }

        return wsUrlNode.asText();
    }

    public void close(LaunchResult result) {
        if (result == null) return;

        // Try to delete session gracefully
        try {
            String deleteUrl = "http://localhost:" + result.port() + "/session/" + extractSessionId(result.wsUrl());
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(deleteUrl))
                .timeout(Duration.ofSeconds(5))
                .DELETE()
                .build();
            httpClient.send(request, HttpResponse.BodyHandlers.discarding());
        } catch (Exception e) {
            // Ignore
        }

        // Wait a bit then kill
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        ProcessManager.kill(result.chromedriverProcess());
    }

    private String extractSessionId(String wsUrl) {
        // ws://localhost:port/session/SESSION_ID
        int idx = wsUrl.lastIndexOf("/session/");
        if (idx >= 0) {
            String rest = wsUrl.substring(idx + 9);
            int endIdx = rest.indexOf("/");
            return endIdx >= 0 ? rest.substring(0, endIdx) : rest;
        }
        return "";
    }
}

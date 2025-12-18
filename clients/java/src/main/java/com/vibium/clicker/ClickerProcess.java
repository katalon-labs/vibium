package com.vibium.clicker;

import com.vibium.exceptions.TimeoutException;
import com.vibium.exceptions.VibiumException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Manages the clicker subprocess.
 * Mirrors clients/javascript/src/clicker/process.ts
 */
public class ClickerProcess {
    private static final Logger logger = LoggerFactory.getLogger(ClickerProcess.class);
    private static final Pattern PORT_PATTERN = Pattern.compile("Server listening on ws://localhost:(\\d+)");
    private static final int DEFAULT_TIMEOUT_MS = 10000;

    private final Process process;
    private final int port;
    private volatile boolean stopped = false;

    private ClickerProcess(Process process, int port) {
        this.process = process;
        this.port = port;
    }

    /**
     * Get the port the server is listening on.
     */
    public int getPort() {
        return port;
    }

    /**
     * Start a clicker process.
     *
     * @param binaryPath Path to the clicker binary
     * @param port Port to listen on (0 for auto-select)
     * @param headless Whether to run headless
     * @return The started ClickerProcess
     */
    public static ClickerProcess start(String binaryPath, int port, boolean headless) {
        List<String> command = new ArrayList<>();
        command.add(binaryPath);
        command.add("serve");

        if (port > 0) {
            command.add("--port");
            command.add(String.valueOf(port));
        }

        if (!headless) {
            command.add("--headed");
        }

        logger.debug("Starting clicker: {}", String.join(" ", command));

        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            // Wait for the server to start and extract the port
            AtomicInteger actualPort = new AtomicInteger(-1);
            AtomicReference<String> output = new AtomicReference<>("");
            CountDownLatch portLatch = new CountDownLatch(1);

            Thread outputReader = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream()))) {
                    String line;
                    StringBuilder sb = new StringBuilder();
                    while ((line = reader.readLine()) != null) {
                        logger.trace("clicker: {}", line);
                        sb.append(line).append("\n");
                        output.set(sb.toString());

                        Matcher matcher = PORT_PATTERN.matcher(line);
                        if (matcher.find() && portLatch.getCount() > 0) {
                            actualPort.set(Integer.parseInt(matcher.group(1)));
                            portLatch.countDown();
                        }
                    }
                } catch (IOException e) {
                    logger.error("Error reading clicker output", e);
                }
            });
            outputReader.setDaemon(true);
            outputReader.start();

            // Wait for the port to be available
            if (!portLatch.await(DEFAULT_TIMEOUT_MS, TimeUnit.MILLISECONDS)) {
                process.destroyForcibly();
                throw new TimeoutException(
                    "Timeout waiting for clicker to start. Output:\n" + output.get()
                );
            }

            // Check if process is still alive
            if (!process.isAlive()) {
                throw new VibiumException(
                    "clicker exited with code " + process.exitValue() + ". Output:\n" + output.get()
                );
            }

            logger.info("Clicker started on port {}", actualPort.get());
            return new ClickerProcess(process, actualPort.get());

        } catch (IOException e) {
            throw new VibiumException("Failed to start clicker", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new VibiumException("Interrupted while starting clicker", e);
        }
    }

    /**
     * Stop the clicker process.
     */
    public void stop() {
        if (stopped) {
            return;
        }
        stopped = true;

        logger.debug("Stopping clicker process");

        // Try graceful shutdown first
        process.destroy();

        try {
            // Wait for graceful shutdown
            boolean terminated = process.waitFor(3, TimeUnit.SECONDS);
            if (!terminated) {
                // Force kill
                logger.debug("Force killing clicker process");
                process.destroyForcibly();
                process.waitFor(2, TimeUnit.SECONDS);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            process.destroyForcibly();
        }
    }

    /**
     * Check if the process is still running.
     */
    public boolean isRunning() {
        return !stopped && process.isAlive();
    }
}

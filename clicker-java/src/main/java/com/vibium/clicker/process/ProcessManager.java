package com.vibium.clicker.process;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ProcessManager {

    private static final Set<Process> processes = ConcurrentHashMap.newKeySet();
    private static volatile boolean shutdownHookRegistered = false;

    public static void register(Process process) {
        ensureShutdownHook();
        processes.add(process);
    }

    public static void unregister(Process process) {
        processes.remove(process);
    }

    public static void kill(Process process) {
        if (process == null) return;

        processes.remove(process);

        // Try graceful shutdown first
        process.destroy();

        try {
            // Wait briefly for graceful shutdown
            boolean exited = process.waitFor(java.util.concurrent.TimeUnit.MILLISECONDS.toNanos(500),
                java.util.concurrent.TimeUnit.NANOSECONDS);
            if (!exited) {
                // Force kill
                process.destroyForcibly();
            }
        } catch (InterruptedException e) {
            process.destroyForcibly();
            Thread.currentThread().interrupt();
        }
    }

    public static void killAll() {
        for (Process p : processes) {
            try {
                p.destroyForcibly();
            } catch (Exception e) {
                // Ignore
            }
        }
        processes.clear();
    }

    private static synchronized void ensureShutdownHook() {
        if (!shutdownHookRegistered) {
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                killAll();
            }, "ProcessManager-Shutdown"));
            shutdownHookRegistered = true;
        }
    }

    public static void waitForSignal() {
        // Block until interrupted
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

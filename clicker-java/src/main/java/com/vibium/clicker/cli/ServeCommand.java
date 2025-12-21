package com.vibium.clicker.cli;

import com.vibium.clicker.process.ProcessManager;
import com.vibium.clicker.proxy.ProxyServer;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
    name = "serve",
    description = "Start WebSocket proxy server"
)
public class ServeCommand implements Runnable {

    @Option(names = {"-p", "--port"}, description = "Port to listen on", defaultValue = "9515")
    private int port;

    @Override
    public void run() {
        try {
            System.out.println("Starting BiDi proxy server on port " + port + "...");
            System.out.println("Headless mode: " + ClickerCommand.isHeadless());
            System.out.println();

            ProxyServer server = new ProxyServer(port, ClickerCommand.isHeadless(), ClickerCommand.verbose);
            server.start();

            System.out.println("Proxy server running at ws://localhost:" + port);
            System.out.println("Press Ctrl+C to stop...");
            System.out.println();

            // Wait for shutdown signal
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\nShutting down...");
                try {
                    server.stop(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                ProcessManager.killAll();
            }));

            // Block forever
            Thread.currentThread().join();

        } catch (Exception e) {
            System.err.println("Server failed: " + e.getMessage());
            if (ClickerCommand.verbose) {
                e.printStackTrace();
            }
            System.exit(1);
        }
    }
}

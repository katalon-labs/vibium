package com.vibium.clicker.cli;

import com.vibium.clicker.bidi.BiDiConnection;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

@Command(
    name = "ws-test",
    description = "Interactive WebSocket test client"
)
public class WsTestCommand implements Runnable {

    @Parameters(index = "0", description = "WebSocket URL")
    private String url;

    @Override
    public void run() {
        try {
            System.out.println("Connecting to " + url + "...");
            BiDiConnection conn = BiDiConnection.connect(url);
            conn.setVerbose(ClickerCommand.verbose);

            System.out.println("Connected! Type messages to send, Ctrl+C to exit.");
            System.out.println();

            // Start receiver thread
            Thread receiver = new Thread(() -> {
                try {
                    while (conn.isOpen()) {
                        String msg = conn.receiveMessage(100, TimeUnit.MILLISECONDS);
                        if (msg != null) {
                            System.out.println("< " + msg);
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            receiver.setDaemon(true);
            receiver.start();

            // Read from stdin and send
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            String line;
            while ((line = reader.readLine()) != null) {
                conn.sendMessage(line);
            }

            conn.closeBlocking();
        } catch (Exception e) {
            System.err.println("WebSocket error: " + e.getMessage());
            if (ClickerCommand.verbose) {
                e.printStackTrace();
            }
            System.exit(1);
        }
    }
}

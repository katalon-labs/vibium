package com.vibium.clicker.proxy;

import com.vibium.clicker.bidi.BiDiConnection;
import com.vibium.clicker.browser.BrowserLauncher;
import com.vibium.clicker.browser.LaunchResult;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class ProxyServer extends WebSocketServer {

    private static final AtomicLong clientIdGenerator = new AtomicLong(0);

    private final boolean headless;
    private final boolean verbose;
    private final BrowserLauncher launcher;
    private final Map<WebSocket, ClientSession> sessions = new ConcurrentHashMap<>();

    public ProxyServer(int port, boolean headless, boolean verbose) {
        super(new InetSocketAddress(port));
        this.headless = headless;
        this.verbose = verbose;
        this.launcher = new BrowserLauncher(verbose);
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        long clientId = clientIdGenerator.incrementAndGet();
        log("[proxy] Client " + clientId + " connected");

        try {
            // Launch browser for this client
            LaunchResult launchResult = launcher.launch(headless);
            log("[proxy] Browser launched for client " + clientId + ": " + launchResult.wsUrl());

            // Connect to browser BiDi WebSocket
            BiDiConnection bidiConn = BiDiConnection.connect(launchResult.wsUrl());
            bidiConn.setVerbose(verbose);

            ClientSession session = new ClientSession(clientId, launchResult, bidiConn);
            sessions.put(conn, session);

            // Start forwarding browser messages to client
            Thread forwarder = new Thread(() -> {
                try {
                    while (bidiConn.isOpen() && conn.isOpen()) {
                        String msg = bidiConn.receiveMessage(100, TimeUnit.MILLISECONDS);
                        if (msg != null && conn.isOpen()) {
                            if (verbose) {
                                log("[proxy] browser -> client " + clientId + ": " + truncate(msg));
                            }
                            conn.send(msg);
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                    if (verbose) {
                        log("[proxy] Forwarder error for client " + clientId + ": " + e.getMessage());
                    }
                }
            }, "BiDi-Forwarder-" + clientId);
            forwarder.setDaemon(true);
            forwarder.start();

        } catch (Exception e) {
            log("[proxy] Failed to launch browser for client " + clientId + ": " + e.getMessage());
            sendError(conn, "Failed to launch browser: " + e.getMessage());
            conn.close();
        }
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        ClientSession session = sessions.get(conn);
        if (session == null) {
            return;
        }

        if (verbose) {
            log("[proxy] client " + session.clientId + " -> browser: " + truncate(message));
        }

        try {
            session.bidiConnection.sendMessage(message);
        } catch (Exception e) {
            log("[proxy] Failed to forward message: " + e.getMessage());
        }
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        ClientSession session = sessions.remove(conn);
        if (session != null) {
            log("[proxy] Client " + session.clientId + " disconnected: " + reason);
            cleanup(session);
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        log("[proxy] Error: " + ex.getMessage());
        if (conn != null) {
            ClientSession session = sessions.remove(conn);
            if (session != null) {
                cleanup(session);
            }
        }
    }

    @Override
    public void onStart() {
        log("[proxy] Server started on port " + getPort());
    }

    private void cleanup(ClientSession session) {
        log("[proxy] Cleaning up session for client " + session.clientId);

        try {
            session.bidiConnection.closeBlocking();
        } catch (Exception e) {
            // Ignore
        }

        launcher.close(session.launchResult);
        log("[proxy] Browser closed for client " + session.clientId);
    }

    private void sendError(WebSocket conn, String message) {
        String errorJson = String.format(
            "{\"error\":{\"error\":\"unknown error\",\"message\":\"%s\"}}",
            message.replace("\"", "\\\"")
        );
        conn.send(errorJson);
    }

    private void log(String msg) {
        System.out.println(msg);
    }

    private String truncate(String s) {
        if (s.length() > 200) {
            return s.substring(0, 200) + "...";
        }
        return s;
    }

    private static class ClientSession {
        final long clientId;
        final LaunchResult launchResult;
        final BiDiConnection bidiConnection;

        ClientSession(long clientId, LaunchResult launchResult, BiDiConnection bidiConnection) {
            this.clientId = clientId;
            this.launchResult = launchResult;
            this.bidiConnection = bidiConnection;
        }
    }
}

package com.vibium.bidi;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.vibium.exceptions.ConnectionException;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * WebSocket connection to a BiDi server.
 * Mirrors clients/javascript/src/bidi/connection.ts
 */
public class BiDiConnection {
    private static final Logger logger = LoggerFactory.getLogger(BiDiConnection.class);

    private final VibiumWebSocketClient client;
    private volatile boolean closed = false;

    private BiDiConnection(VibiumWebSocketClient client) {
        this.client = client;
        client.setConnection(this);
    }

    /**
     * Connect to a BiDi WebSocket server.
     *
     * @param url The WebSocket URL (e.g., "ws://localhost:9222")
     * @return A connected BiDiConnection
     * @throws ConnectionException if the connection fails
     */
    public static BiDiConnection connect(String url) {
        return connect(url, 10000);
    }

    /**
     * Connect to a BiDi WebSocket server with a timeout.
     *
     * @param url The WebSocket URL
     * @param timeoutMs Connection timeout in milliseconds
     * @return A connected BiDiConnection
     * @throws ConnectionException if the connection fails
     */
    public static BiDiConnection connect(String url, int timeoutMs) {
        try {
            URI uri = new URI(url);
            VibiumWebSocketClient wsClient = new VibiumWebSocketClient(uri);
            BiDiConnection connection = new BiDiConnection(wsClient);

            wsClient.connectBlocking(timeoutMs, TimeUnit.MILLISECONDS);

            if (!wsClient.isOpen()) {
                throw new ConnectionException("Failed to connect to " + url);
            }

            return connection;
        } catch (ConnectionException e) {
            throw e;
        } catch (Exception e) {
            throw new ConnectionException("Failed to connect to " + url, e);
        }
    }

    /**
     * Check if the connection is closed.
     */
    public boolean isClosed() {
        return closed || !client.isOpen();
    }

    /**
     * Set a handler for incoming messages.
     */
    public void onMessage(Consumer<JsonObject> handler) {
        client.setMessageHandler(handler);
    }

    /**
     * Send a message over the WebSocket.
     *
     * @param message The message to send
     * @throws ConnectionException if the connection is closed
     */
    public void send(String message) {
        if (isClosed()) {
            throw new ConnectionException("Connection closed");
        }
        logger.trace("Sending message: {}", message);
        client.send(message);
    }

    /**
     * Close the connection.
     */
    public void close() {
        if (!closed) {
            closed = true;
            try {
                client.closeBlocking();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    void markClosed() {
        this.closed = true;
    }

    /**
     * Internal WebSocket client implementation.
     */
    private static class VibiumWebSocketClient extends WebSocketClient {
        private static final Logger logger = LoggerFactory.getLogger(VibiumWebSocketClient.class);

        private BiDiConnection connection;
        private Consumer<JsonObject> messageHandler;

        VibiumWebSocketClient(URI serverUri) {
            super(serverUri);
        }

        void setConnection(BiDiConnection connection) {
            this.connection = connection;
        }

        void setMessageHandler(Consumer<JsonObject> handler) {
            this.messageHandler = handler;
        }

        @Override
        public void onOpen(ServerHandshake handshake) {
            logger.debug("WebSocket connected");
        }

        @Override
        public void onMessage(String message) {
            logger.trace("Received message: {}", message);
            try {
                JsonObject json = JsonParser.parseString(message).getAsJsonObject();
                if (messageHandler != null) {
                    messageHandler.accept(json);
                }
            } catch (Exception e) {
                logger.error("Failed to parse BiDi message: {}", e.getMessage());
            }
        }

        @Override
        public void onClose(int code, String reason, boolean remote) {
            logger.debug("WebSocket closed: code={}, reason={}, remote={}", code, reason, remote);
            if (connection != null) {
                connection.markClosed();
            }
        }

        @Override
        public void onError(Exception ex) {
            logger.error("WebSocket error: {}", ex.getMessage());
        }
    }
}

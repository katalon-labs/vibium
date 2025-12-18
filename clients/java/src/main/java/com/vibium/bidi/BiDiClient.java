package com.vibium.bidi;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.vibium.exceptions.ConnectionException;
import com.vibium.exceptions.VibiumException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * BiDi protocol client for sending commands and receiving responses.
 * Mirrors clients/javascript/src/bidi/client.ts
 */
public class BiDiClient {
    private static final Logger logger = LoggerFactory.getLogger(BiDiClient.class);
    private static final Gson gson = new Gson();
    private static final int DEFAULT_TIMEOUT_MS = 30000;

    private final BiDiConnection connection;
    private final AtomicInteger nextId = new AtomicInteger(1);
    private final Map<Integer, CompletableFuture<JsonObject>> pendingCommands = new ConcurrentHashMap<>();
    private Consumer<BiDiEvent> eventHandler;

    private BiDiClient(BiDiConnection connection) {
        this.connection = connection;

        connection.onMessage(this::handleMessage);
    }

    /**
     * Connect to a BiDi server.
     *
     * @param url The WebSocket URL
     * @return A connected BiDiClient
     */
    public static BiDiClient connect(String url) {
        BiDiConnection connection = BiDiConnection.connect(url);
        return new BiDiClient(connection);
    }

    /**
     * Connect to a BiDi server with a timeout.
     *
     * @param url The WebSocket URL
     * @param timeoutMs Connection timeout in milliseconds
     * @return A connected BiDiClient
     */
    public static BiDiClient connect(String url, int timeoutMs) {
        BiDiConnection connection = BiDiConnection.connect(url, timeoutMs);
        return new BiDiClient(connection);
    }

    private void handleMessage(JsonObject json) {
        // Check if it's a response (has "id" field)
        if (json.has("id")) {
            handleResponse(json);
        } else if (json.has("method")) {
            handleEvent(json);
        }
    }

    private void handleResponse(JsonObject json) {
        int id = json.get("id").getAsInt();
        CompletableFuture<JsonObject> pending = pendingCommands.remove(id);

        if (pending == null) {
            logger.warn("Received response for unknown command: {}", id);
            return;
        }

        String type = json.has("type") ? json.get("type").getAsString() : "success";

        if ("error".equals(type)) {
            JsonObject errorObj = json.getAsJsonObject("error");
            String errorCode = errorObj != null && errorObj.has("error")
                ? errorObj.get("error").getAsString()
                : "unknown";
            String errorMessage = errorObj != null && errorObj.has("message")
                ? errorObj.get("message").getAsString()
                : "Unknown error";
            pending.completeExceptionally(new VibiumException(errorCode + ": " + errorMessage));
        } else {
            JsonObject result = json.has("result") ? json.getAsJsonObject("result") : new JsonObject();
            pending.complete(result);
        }
    }

    private void handleEvent(JsonObject json) {
        if (eventHandler != null) {
            String method = json.get("method").getAsString();
            JsonObject params = json.has("params") ? json.getAsJsonObject("params") : new JsonObject();
            eventHandler.accept(new BiDiEvent(method, params));
        }
    }

    /**
     * Set a handler for BiDi events.
     */
    public void onEvent(Consumer<BiDiEvent> handler) {
        this.eventHandler = handler;
    }

    /**
     * Send a BiDi command and wait for a response.
     *
     * @param method The BiDi method name
     * @param params The command parameters
     * @return The result as a JsonObject
     */
    public JsonObject send(String method, Map<String, Object> params) {
        return send(method, params, DEFAULT_TIMEOUT_MS);
    }

    /**
     * Send a BiDi command and wait for a response with a timeout.
     *
     * @param method The BiDi method name
     * @param params The command parameters
     * @param timeoutMs Timeout in milliseconds
     * @return The result as a JsonObject
     */
    public JsonObject send(String method, Map<String, Object> params, int timeoutMs) {
        int id = nextId.getAndIncrement();
        BiDiCommand command = new BiDiCommand(id, method, params != null ? params : new HashMap<>());

        CompletableFuture<JsonObject> future = new CompletableFuture<>();
        pendingCommands.put(id, future);

        try {
            String json = gson.toJson(command);
            connection.send(json);

            return future.get(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (java.util.concurrent.TimeoutException e) {
            pendingCommands.remove(id);
            throw new com.vibium.exceptions.TimeoutException("Command timed out: " + method);
        } catch (VibiumException e) {
            throw e;
        } catch (Exception e) {
            pendingCommands.remove(id);
            if (e.getCause() instanceof VibiumException) {
                throw (VibiumException) e.getCause();
            }
            throw new VibiumException("Command failed: " + method, e);
        }
    }

    /**
     * Send a BiDi command with no parameters.
     *
     * @param method The BiDi method name
     * @return The result as a JsonObject
     */
    public JsonObject send(String method) {
        return send(method, null);
    }

    /**
     * Send a BiDi command and convert the result to a specific type.
     *
     * @param method The BiDi method name
     * @param params The command parameters
     * @param resultType The class to convert the result to
     * @return The result converted to the specified type
     */
    public <T> T send(String method, Map<String, Object> params, Class<T> resultType) {
        JsonObject result = send(method, params);
        return gson.fromJson(result, resultType);
    }

    /**
     * Close the client and underlying connection.
     */
    public void close() {
        // Reject all pending commands
        for (var entry : pendingCommands.entrySet()) {
            entry.getValue().completeExceptionally(new ConnectionException("Connection closed"));
        }
        pendingCommands.clear();

        connection.close();
    }

    /**
     * Check if the client is connected.
     */
    public boolean isConnected() {
        return !connection.isClosed();
    }
}

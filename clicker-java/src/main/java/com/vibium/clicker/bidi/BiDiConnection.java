package com.vibium.clicker.bidi;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class BiDiConnection extends WebSocketClient {

    private final BlockingQueue<String> messageQueue = new LinkedBlockingQueue<>();
    private final CountDownLatch connectLatch = new CountDownLatch(1);
    private volatile Exception connectError;
    private volatile boolean verbose = false;

    public BiDiConnection(URI serverUri) {
        super(serverUri);
    }

    public static BiDiConnection connect(String url) throws Exception {
        return connect(url, 10000);
    }

    public static BiDiConnection connect(String url, int timeoutMs) throws Exception {
        BiDiConnection conn = new BiDiConnection(new URI(url));
        conn.connectBlocking(timeoutMs, TimeUnit.MILLISECONDS);

        if (!conn.isOpen()) {
            if (conn.connectError != null) {
                throw conn.connectError;
            }
            throw new Exception("Failed to connect to " + url);
        }

        return conn;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    @Override
    public void onOpen(ServerHandshake handshake) {
        if (verbose) {
            System.err.println("[BiDi] Connected");
        }
        connectLatch.countDown();
    }

    @Override
    public void onMessage(String message) {
        if (verbose) {
            System.err.println("[BiDi] <- " + message);
        }
        messageQueue.offer(message);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        if (verbose) {
            System.err.println("[BiDi] Connection closed: " + reason);
        }
        connectLatch.countDown();
    }

    @Override
    public void onError(Exception ex) {
        if (verbose) {
            System.err.println("[BiDi] Error: " + ex.getMessage());
        }
        connectError = ex;
        connectLatch.countDown();
    }

    public void sendMessage(String message) {
        if (verbose) {
            System.err.println("[BiDi] -> " + message);
        }
        send(message);
    }

    public String receiveMessage() throws InterruptedException {
        return messageQueue.take();
    }

    public String receiveMessage(long timeout, TimeUnit unit) throws InterruptedException {
        return messageQueue.poll(timeout, unit);
    }

    public void clearQueue() {
        messageQueue.clear();
    }
}

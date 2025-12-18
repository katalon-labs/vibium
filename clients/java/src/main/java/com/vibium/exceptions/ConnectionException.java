package com.vibium.exceptions;

/**
 * Exception thrown when a connection error occurs.
 */
public class ConnectionException extends VibiumException {

    public ConnectionException(String message) {
        super(message);
    }

    public ConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}

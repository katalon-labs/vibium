package com.vibium.exceptions;

/**
 * Exception thrown when an operation times out.
 */
public class TimeoutException extends VibiumException {

    public TimeoutException(String message) {
        super(message);
    }

    public TimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
}

package com.vibium.exceptions;

/**
 * Base exception for all Vibium errors.
 */
public class VibiumException extends RuntimeException {

    public VibiumException(String message) {
        super(message);
    }

    public VibiumException(String message, Throwable cause) {
        super(message, cause);
    }
}

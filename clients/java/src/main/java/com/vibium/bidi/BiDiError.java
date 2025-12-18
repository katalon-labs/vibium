package com.vibium.bidi;

/**
 * A BiDi protocol error.
 * Mirrors clients/javascript/src/bidi/types.ts BiDiError
 */
public record BiDiError(
    String error,
    String message,
    String stacktrace
) {}

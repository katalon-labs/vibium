package com.vibium.bidi.types;

/**
 * Result of a screenshot command.
 * Mirrors clients/javascript/src/bidi/types.ts ScreenshotResult
 */
public record ScreenshotResult(
    String data  // base64 encoded PNG
) {}

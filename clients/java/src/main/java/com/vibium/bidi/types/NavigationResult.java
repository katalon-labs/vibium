package com.vibium.bidi.types;

/**
 * Result of a navigation command.
 * Mirrors clients/javascript/src/bidi/types.ts NavigationResult
 */
public record NavigationResult(
    String navigation,
    String url
) {}

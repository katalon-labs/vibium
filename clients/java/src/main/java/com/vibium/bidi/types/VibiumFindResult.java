package com.vibium.bidi.types;

/**
 * Result from vibium:find command.
 * Mirrors the response structure from clicker's vibium:find handler.
 */
public record VibiumFindResult(
    String tag,
    String text,
    BoundingBox box
) {}

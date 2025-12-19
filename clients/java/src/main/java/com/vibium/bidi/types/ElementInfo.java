package com.vibium.bidi.types;

/**
 * Information about an element found on the page.
 * Mirrors ElementInfo interface in clients/javascript/src/element.ts
 */
public record ElementInfo(
    String tag,
    String text,
    BoundingBox box
) {}

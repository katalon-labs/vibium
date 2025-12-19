package com.vibium.bidi.types;

/**
 * Represents the bounding box of an element.
 * Mirrors BoundingBox interface in clients/javascript/src/element.ts
 */
public record BoundingBox(
    double x,
    double y,
    double width,
    double height
) {}

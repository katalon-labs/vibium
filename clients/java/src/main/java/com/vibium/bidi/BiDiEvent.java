package com.vibium.bidi;

import com.google.gson.JsonObject;

/**
 * A BiDi protocol event.
 * Mirrors clients/javascript/src/bidi/types.ts BiDiEvent
 */
public record BiDiEvent(
    String method,
    JsonObject params
) {}

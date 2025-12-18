package com.vibium.bidi;

import java.util.Map;

/**
 * A BiDi protocol command.
 * Mirrors clients/javascript/src/bidi/types.ts BiDiCommand
 */
public record BiDiCommand(
    int id,
    String method,
    Map<String, Object> params
) {}

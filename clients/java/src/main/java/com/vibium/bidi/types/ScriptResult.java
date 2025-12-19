package com.vibium.bidi.types;

/**
 * Result from a script.callFunction BiDi command.
 * Mirrors ScriptResult interface in clients/javascript/src/element.ts
 */
public record ScriptResult(
    String type,
    ScriptResultValue result
) {
    public record ScriptResultValue(
        String type,
        Object value
    ) {}
}

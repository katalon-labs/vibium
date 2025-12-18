package com.vibium.bidi;

import com.google.gson.JsonObject;

/**
 * A BiDi protocol response.
 * Mirrors clients/javascript/src/bidi/types.ts BiDiResponse
 */
public record BiDiResponse(
    int id,
    String type,
    JsonObject result,
    BiDiError error
) {
    public boolean isSuccess() {
        return "success".equals(type);
    }

    public boolean isError() {
        return "error".equals(type);
    }
}

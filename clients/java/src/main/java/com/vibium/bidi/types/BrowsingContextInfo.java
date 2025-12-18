package com.vibium.bidi.types;

import java.util.List;

/**
 * Information about a browsing context.
 * Mirrors clients/javascript/src/bidi/types.ts BrowsingContextInfo
 */
public record BrowsingContextInfo(
    String context,
    String url,
    List<BrowsingContextInfo> children,
    String parent
) {}

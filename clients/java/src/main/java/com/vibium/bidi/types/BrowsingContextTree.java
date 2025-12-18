package com.vibium.bidi.types;

import java.util.List;

/**
 * A tree of browsing contexts.
 * Mirrors clients/javascript/src/bidi/types.ts BrowsingContextTree
 */
public record BrowsingContextTree(
    List<BrowsingContextInfo> contexts
) {}

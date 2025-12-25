package com.vibium;

/**
 * Options for finding elements.
 * Mirrors clients/javascript/src/vibe.ts FindOptions
 */
public class FindOptions {
    private int timeout = 30000;

    /**
     * Create default find options (30s timeout).
     */
    public FindOptions() {
    }

    /**
     * Set the timeout for waiting for the element.
     *
     * @param timeout Timeout in milliseconds (default: 30000)
     * @return this for chaining
     */
    public FindOptions timeout(int timeout) {
        this.timeout = timeout;
        return this;
    }

    public int getTimeout() {
        return timeout;
    }
}

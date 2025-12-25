package com.vibium;

/**
 * Options for element actions (click, type, etc).
 * Mirrors clients/javascript/src/element.ts ActionOptions
 */
public class ActionOptions {
    private int timeout = 30000;

    /**
     * Create default action options (30s timeout).
     */
    public ActionOptions() {
    }

    /**
     * Set the timeout for actionability checks.
     *
     * @param timeout Timeout in milliseconds (default: 30000)
     * @return this for chaining
     */
    public ActionOptions timeout(int timeout) {
        this.timeout = timeout;
        return this;
    }

    public int getTimeout() {
        return timeout;
    }
}

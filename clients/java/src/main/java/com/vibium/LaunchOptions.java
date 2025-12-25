package com.vibium;

/**
 * Options for launching a browser.
 * Mirrors clients/javascript/src/browser.ts LaunchOptions
 */
public class LaunchOptions {
    private boolean headless = false;
    private int port = 0;
    private String executablePath;

    /**
     * Create default launch options (visible browser).
     */
    public LaunchOptions() {
    }

    /**
     * Set headless mode.
     *
     * @param headless true for headless, false to show browser window
     * @return this for chaining
     */
    public LaunchOptions headless(boolean headless) {
        this.headless = headless;
        return this;
    }

    /**
     * Set the port for the clicker server.
     *
     * @param port The port to listen on (0 for auto-select)
     * @return this for chaining
     */
    public LaunchOptions port(int port) {
        this.port = port;
        return this;
    }

    /**
     * Set the path to the clicker executable.
     *
     * @param executablePath Path to the clicker binary
     * @return this for chaining
     */
    public LaunchOptions executablePath(String executablePath) {
        this.executablePath = executablePath;
        return this;
    }

    public boolean isHeadless() {
        return headless;
    }

    public int getPort() {
        return port;
    }

    public String getExecutablePath() {
        return executablePath;
    }
}

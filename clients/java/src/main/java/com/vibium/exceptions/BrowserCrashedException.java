package com.vibium.exceptions;

/**
 * Exception thrown when the browser process crashes unexpectedly.
 * Mirrors clients/javascript/src/utils/errors.ts BrowserCrashedError
 */
public class BrowserCrashedException extends VibiumException {
    private final int exitCode;
    private final String output;

    public BrowserCrashedException(int exitCode) {
        this(exitCode, null);
    }

    public BrowserCrashedException(int exitCode, String output) {
        super(output != null
            ? "Browser crashed with exit code " + exitCode + ": " + output
            : "Browser crashed with exit code " + exitCode);
        this.exitCode = exitCode;
        this.output = output;
    }

    public int getExitCode() {
        return exitCode;
    }

    public String getOutput() {
        return output;
    }
}

package com.vibium.clicker;

/**
 * OS and architecture detection utilities.
 * Mirrors clients/javascript/src/clicker/platform.ts
 */
public final class Platform {

    public enum OS {
        LINUX("linux"),
        DARWIN("darwin"),
        WINDOWS("win32");

        private final String identifier;

        OS(String identifier) {
            this.identifier = identifier;
        }

        public String getIdentifier() {
            return identifier;
        }
    }

    public enum Arch {
        X64("x64"),
        ARM64("arm64");

        private final String identifier;

        Arch(String identifier) {
            this.identifier = identifier;
        }

        public String getIdentifier() {
            return identifier;
        }
    }

    private Platform() {
        // Utility class
    }

    /**
     * Get the current operating system.
     */
    public static OS getOS() {
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("linux")) {
            return OS.LINUX;
        } else if (osName.contains("mac") || osName.contains("darwin")) {
            return OS.DARWIN;
        } else if (osName.contains("windows")) {
            return OS.WINDOWS;
        }
        throw new UnsupportedOperationException("Unsupported platform: " + osName);
    }

    /**
     * Get the current CPU architecture.
     */
    public static Arch getArch() {
        String arch = System.getProperty("os.arch").toLowerCase();
        if (arch.equals("amd64") || arch.equals("x86_64")) {
            return Arch.X64;
        } else if (arch.equals("aarch64") || arch.equals("arm64")) {
            return Arch.ARM64;
        }
        throw new UnsupportedOperationException("Unsupported architecture: " + arch);
    }

    /**
     * Get the platform identifier string (e.g., "darwin-arm64").
     */
    public static String getPlatformIdentifier() {
        return getOS().getIdentifier() + "-" + getArch().getIdentifier();
    }

    /**
     * Get the binary name for the current platform.
     */
    public static String getBinaryName() {
        return getOS() == OS.WINDOWS ? "clicker.exe" : "clicker";
    }
}

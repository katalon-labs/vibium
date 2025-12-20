package com.vibium.clicker;

import com.vibium.exceptions.VibiumException;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Resolves the path to the clicker binary.
 * Mirrors clients/javascript/src/clicker/binary.ts
 *
 * Search order:
 * 1. Explicit path (if provided)
 * 2. CLICKER_PATH environment variable
 * 3. Local development paths relative to CWD
 */
public final class BinaryResolver {

    private BinaryResolver() {
        // Utility class
    }

    /**
     * Resolve the clicker binary path.
     *
     * @param explicitPath Optional explicit path to the binary
     * @return The resolved path to the clicker binary
     * @throws VibiumException if the binary cannot be found
     */
    public static String resolve(String explicitPath) {
        // 1. Check explicit path
        if (explicitPath != null && !explicitPath.isEmpty()) {
            if (Files.exists(Paths.get(explicitPath))) {
                return explicitPath;
            }
            throw new VibiumException("Clicker binary not found at explicit path: " + explicitPath);
        }

        // 2. Check CLICKER_PATH environment variable
        String envPath = System.getenv("CLICKER_PATH");
        if (envPath != null && !envPath.isEmpty() && Files.exists(Paths.get(envPath))) {
            return envPath;
        }

        String binaryName = Platform.getBinaryName();
        String cwd = System.getProperty("user.dir");

        // 3. Check local development paths
        String[] localPaths = {
            // From vibium/ root
            Paths.get(cwd, "clicker", "bin", binaryName).toString(),
            // From clients/java/
            Paths.get(cwd, "..", "..", "clicker", "bin", binaryName).normalize().toString(),
            // From bin/ in current directory
            Paths.get(cwd, "bin", binaryName).toString(),
        };

        for (String localPath : localPaths) {
            Path path = Paths.get(localPath);
            if (Files.exists(path) && Files.isExecutable(path)) {
                return path.toAbsolutePath().toString();
            }
        }

        // 4. Check PATH
        String pathEnv = System.getenv("PATH");
        if (pathEnv != null) {
            String pathSeparator = File.pathSeparator;
            for (String dir : pathEnv.split(pathSeparator)) {
                Path binaryPath = Paths.get(dir, binaryName);
                if (Files.exists(binaryPath) && Files.isExecutable(binaryPath)) {
                    return binaryPath.toAbsolutePath().toString();
                }
            }
        }

        throw new VibiumException(
            "Could not find clicker binary. " +
            "Set CLICKER_PATH environment variable or ensure 'clicker' is in PATH"
        );
    }

    /**
     * Resolve the clicker binary path using default search order.
     */
    public static String resolve() {
        return resolve(null);
    }
}

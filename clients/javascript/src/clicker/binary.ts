import fs from 'fs';
import path from 'path';
import { getPlatform, getArch } from './platform';

/**
 * Resolve the path to the clicker binary.
 *
 * Search order:
 * 1. CLICKER_PATH environment variable
 * 2. Platform-specific npm package (@vibium/clicker-{platform}-{arch})
 * 3. Local development path (../../clicker/bin/clicker)
 */
export function getClickerPath(): string {
  // 1. Check environment variable
  const envPath = process.env.CLICKER_PATH;
  if (envPath && fs.existsSync(envPath)) {
    return envPath;
  }

  // 2. Check platform-specific npm package
  const platform = getPlatform();
  const arch = getArch();
  const packageName = `@vibium/clicker-${platform}-${arch}`;

  try {
    // Try to resolve the platform package
    const packagePath = require.resolve(`${packageName}/package.json`);
    const packageDir = path.dirname(packagePath);
    const binaryName = platform === 'win32' ? 'clicker.exe' : 'clicker';
    const binaryPath = path.join(packageDir, 'bin', binaryName);

    if (fs.existsSync(binaryPath)) {
      return binaryPath;
    }
  } catch {
    // Package not installed, continue to fallback
  }

  // 3. Check local development path
  const localPaths = [
    // From clients/javascript, go up to find clicker/bin/clicker
    path.resolve(__dirname, '..', '..', '..', '..', 'clicker', 'bin', 'clicker'),
    // From dist folder
    path.resolve(__dirname, '..', '..', '..', '..', '..', 'clicker', 'bin', 'clicker'),
  ];

  for (const localPath of localPaths) {
    if (fs.existsSync(localPath)) {
      return localPath;
    }
  }

  throw new Error(
    `Could not find clicker binary. ` +
    `Set CLICKER_PATH environment variable or install ${packageName}`
  );
}

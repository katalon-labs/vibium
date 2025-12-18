import os from 'os';

export type Platform = 'linux' | 'darwin' | 'win32';
export type Arch = 'x64' | 'arm64';

export function getPlatform(): Platform {
  const platform = os.platform();
  if (platform === 'linux' || platform === 'darwin' || platform === 'win32') {
    return platform;
  }
  throw new Error(`Unsupported platform: ${platform}`);
}

export function getArch(): Arch {
  const arch = os.arch();
  if (arch === 'x64' || arch === 'arm64') {
    return arch;
  }
  throw new Error(`Unsupported architecture: ${arch}`);
}

export function getPlatformIdentifier(): string {
  const platform = getPlatform();
  const arch = getArch();
  return `${platform}-${arch}`;
}

#!/bin/bash
set -e

cd "$(dirname "$0")/.."

echo "Building native image..."
mvn clean package -Pnative -DskipTests

# Get platform info
OS=$(uname -s | tr '[:upper:]' '[:lower:]')
ARCH=$(uname -m)

case "$ARCH" in
    x86_64|amd64) ARCH="amd64" ;;
    aarch64|arm64) ARCH="arm64" ;;
esac

case "$OS" in
    linux) OS="linux" ;;
    darwin) OS="darwin" ;;
    mingw*|msys*|cygwin*) OS="windows" ;;
esac

# Create bin directory and copy binary
mkdir -p bin
if [ "$OS" = "windows" ]; then
    cp target/clicker-java.exe "bin/clicker-java-${OS}-${ARCH}.exe"
else
    cp target/clicker-java "bin/clicker-java-${OS}-${ARCH}"
    chmod +x "bin/clicker-java-${OS}-${ARCH}"
fi

echo ""
echo "Build complete!"
ls -la bin/

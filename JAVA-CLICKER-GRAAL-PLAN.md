# Java Clicker with GraalVM Native Image - Implementation Plan

## Overview

This plan outlines the creation of a Java implementation of the clicker binary, compiled to a native executable using GraalVM's `native-image` tool. The goal is to produce a functionally equivalent, fast-starting, low-memory binary.

---

## Why Java + GraalVM?

| Benefit | Description |
|---------|-------------|
| **Ecosystem** | Access to mature Java libraries for WebSocket, HTTP, JSON |
| **Native Performance** | GraalVM native-image produces binaries with ~10-50ms startup |
| **No JVM Required** | Single static binary, no Java installation needed at runtime |
| **Cross-platform** | Build for Linux, macOS, Windows (x64 and arm64) |
| **Memory Efficiency** | Native images use significantly less memory than JVM |

---

## Technology Stack

### Core Dependencies

| Library | Purpose | GraalVM Compatible |
|---------|---------|-------------------|
| **Picocli** | CLI framework (similar to Cobra) | Yes (native-image aware) |
| **Java-WebSocket** | WebSocket client/server | Yes |
| **Jackson** | JSON parsing/serialization | Yes (with reflection config) |
| **SLF4J + Logback** | Logging | Yes |

### Build Tools

| Tool | Purpose |
|------|---------|
| **Maven** or **Gradle** | Build and dependency management |
| **GraalVM native-image** | Compile to native binary |
| **native-image-maven-plugin** | Maven integration for native builds |

---

## Project Structure

```
clicker-java/
├── pom.xml                              # Maven build config with native-image profile
├── src/
│   └── main/
│       ├── java/
│       │   └── com/vibium/clicker/
│       │       ├── Main.java            # Entry point
│       │       ├── cli/                 # CLI commands (Picocli)
│       │       │   ├── ClickerCommand.java       # Root command
│       │       │   ├── VersionCommand.java
│       │       │   ├── PathsCommand.java
│       │       │   ├── InstallCommand.java
│       │       │   ├── LaunchTestCommand.java
│       │       │   ├── NavigateCommand.java
│       │       │   ├── ScreenshotCommand.java
│       │       │   ├── EvalCommand.java
│       │       │   ├── FindCommand.java
│       │       │   ├── ClickCommand.java
│       │       │   ├── TypeCommand.java
│       │       │   ├── ServeCommand.java
│       │       │   └── McpCommand.java
│       │       │
│       │       ├── bidi/                # WebDriver BiDi protocol
│       │       │   ├── BiDiConnection.java       # WebSocket connection wrapper
│       │       │   ├── BiDiClient.java           # High-level BiDi client
│       │       │   ├── BiDiProtocol.java         # Command/Response/Event types
│       │       │   ├── SessionModule.java        # session.* commands
│       │       │   ├── BrowsingContextModule.java # browsingContext.* commands
│       │       │   ├── ScriptModule.java         # script.* commands
│       │       │   ├── InputModule.java          # input.* commands
│       │       │   └── ElementInfo.java          # Element data class
│       │       │
│       │       ├── browser/             # Browser management
│       │       │   ├── BrowserLauncher.java      # Launch chromedriver + Chrome
│       │       │   ├── BrowserInstaller.java     # Download Chrome for Testing
│       │       │   └── LaunchResult.java         # Launch result data class
│       │       │
│       │       ├── proxy/               # WebSocket proxy server
│       │       │   ├── ProxyServer.java          # WebSocket server
│       │       │   └── SessionRouter.java        # Client-browser routing
│       │       │
│       │       ├── paths/               # Platform paths
│       │       │   └── PlatformPaths.java        # OS-specific path resolution
│       │       │
│       │       ├── process/             # Process management
│       │       │   └── ProcessManager.java       # Process tracking + cleanup
│       │       │
│       │       └── mcp/                 # MCP server (future)
│       │           ├── McpServer.java
│       │           ├── McpHandlers.java
│       │           └── McpSchema.java
│       │
│       └── resources/
│           ├── META-INF/
│           │   └── native-image/
│           │       ├── reflect-config.json       # Reflection metadata
│           │       ├── resource-config.json      # Resource metadata
│           │       └── native-image.properties   # Build options
│           └── logback.xml
│
├── scripts/
│   ├── build-native.sh                  # Build native binary
│   └── build-all-platforms.sh           # Cross-compile for all platforms
│
└── bin/                                 # Build output (gitignored)
    ├── clicker-java-linux-amd64
    ├── clicker-java-linux-arm64
    ├── clicker-java-darwin-amd64
    ├── clicker-java-darwin-arm64
    └── clicker-java-windows-amd64.exe
```

---

## Implementation Phases

### Phase 1: Project Setup & CLI Framework

#### 1.1 Maven Project Setup
- Create `pom.xml` with:
  - Java 21 (GraalVM compatible)
  - Picocli for CLI
  - Jackson for JSON
  - Java-WebSocket for WebSocket
  - GraalVM native-image-maven-plugin
- Set up native-image build profile

#### 1.2 Root CLI Command
- Create `ClickerCommand.java` with Picocli annotations
- Global options: `--headed`, `--wait-open`, `--wait-close`
- Implement `version` and `paths` subcommands

**Checkpoint:**
```bash
cd clicker-java
mvn package -Pnative
./bin/clicker-java --version
./bin/clicker-java paths
```

---

### Phase 2: Platform Paths & Browser Installation

#### 2.1 Platform Path Resolution
- Implement `PlatformPaths.java`:
  - Detect OS: Linux, macOS, Windows
  - Cache directory per platform
  - Chrome/chromedriver executable paths
  - System Chrome fallback

#### 2.2 Browser Installer
- Implement `BrowserInstaller.java`:
  - Fetch Chrome for Testing versions JSON
  - Download platform-specific ZIP
  - Extract to cache directory
  - Make executables (chmod on Unix)
  - Respect `VIBIUM_SKIP_BROWSER_DOWNLOAD`

**Checkpoint:**
```bash
./bin/clicker-java install
ls ~/.cache/vibium/chrome-for-testing/
```

---

### Phase 3: WebSocket & BiDi Protocol

#### 3.1 WebSocket Connection
- Implement `BiDiConnection.java`:
  - Wrap Java-WebSocket client
  - Thread-safe send/receive
  - Connect/close lifecycle
  - Message queuing for responses

#### 3.2 BiDi Protocol Types
- Implement `BiDiProtocol.java`:
  - `BiDiCommand`: id, method, params
  - `BiDiResponse`: id, result, error
  - `BiDiEvent`: method, params
  - Atomic ID generator
  - Jackson serialization annotations

#### 3.3 Session Commands
- Implement `SessionModule.java`:
  - `sessionStatus()`
  - `sessionNew()`

**Checkpoint:**
```bash
./bin/clicker-java bidi-test
# Launches Chrome, connects, sends session.status
```

---

### Phase 4: Browser Launching

#### 4.1 Browser Launcher
- Implement `BrowserLauncher.java`:
  - Find available TCP port
  - Start chromedriver as subprocess
  - Wait for chromedriver /status endpoint
  - Create session with BiDi capability
  - Extract WebSocket URL
  - Return `LaunchResult`

#### 4.2 Process Management
- Implement `ProcessManager.java`:
  - Track spawned processes
  - Signal handlers (SIGINT, SIGTERM)
  - Kill process groups on Unix
  - Cleanup on exit

**Checkpoint:**
```bash
./bin/clicker-java launch-test
# Prints: ws://127.0.0.1:xxxxx/session/...
# Ctrl+C should kill Chrome cleanly
```

---

### Phase 5: Navigation & Screenshots

#### 5.1 Browsing Context Module
- Implement `BrowsingContextModule.java`:
  - `getTree()` - Get browsing contexts
  - `navigate(context, url)` - Navigate with wait
  - `captureScreenshot(context)` - Viewport PNG

#### 5.2 Script Module
- Implement `ScriptModule.java`:
  - `getRealms(context)`
  - `evaluate(context, expression)`
  - `callFunction(context, fn, args)`

**Checkpoint:**
```bash
./bin/clicker-java navigate https://example.com
./bin/clicker-java screenshot https://example.com -o shot.png
./bin/clicker-java eval https://example.com "document.title"
```

---

### Phase 6: Element Finding & Input

#### 6.1 Element Finding
- Extend `ScriptModule.java` with element finding:
  - Find by CSS selector via script.callFunction
  - Get tag, text, bounding box
  - Return `ElementInfo` with center calculation

#### 6.2 Input Module
- Implement `InputModule.java`:
  - `performActions(context, actions)`
  - `click(context, x, y)`
  - `clickElement(context, selector)`
  - `doubleClick(context, x, y)`
  - `moveMouse(context, x, y)`
  - `typeText(context, text)`
  - `typeIntoElement(context, selector, text)`
  - `pressKey(context, key)`

**Checkpoint:**
```bash
./bin/clicker-java find https://example.com "a"
./bin/clicker-java click https://example.com "a"
./bin/clicker-java type https://the-internet.herokuapp.com/inputs "input" "12345"
```

---

### Phase 7: WebSocket Proxy Server

#### 7.1 Proxy Server
- Implement `ProxyServer.java`:
  - Listen on configurable port (default 9515)
  - Accept WebSocket upgrade
  - Callback-based API

#### 7.2 Session Router
- Implement `SessionRouter.java`:
  - On client connect: launch browser
  - Connect to browser BiDi WebSocket
  - Bidirectional message forwarding
  - On disconnect: kill browser

**Checkpoint:**
```bash
./bin/clicker-java serve &
websocat ws://localhost:9515
> {"id":1,"method":"session.status","params":{}}
# Returns session status
```

---

### Phase 8: GraalVM Native Image Configuration

#### 8.1 Reflection Configuration
- Create `reflect-config.json` for:
  - Picocli command classes
  - Jackson POJOs (BiDi types)
  - WebSocket callbacks

#### 8.2 Resource Configuration
- Create `resource-config.json` for bundled resources

#### 8.3 Build Optimization
- Configure native-image options:
  - `--no-fallback` (no JVM fallback)
  - `-H:+ReportExceptionStackTraces`
  - `-march=native` for platform optimization
  - Static linking where possible

**Checkpoint:**
```bash
./scripts/build-native.sh
file ./bin/clicker-java-linux-amd64
# Should show statically linked executable
./bin/clicker-java-linux-amd64 --version
# Should start in <50ms
```

---

### Phase 9: Cross-Platform Builds

#### 9.1 Build Matrix
Create CI/CD configuration for:
- Linux x64 (Ubuntu)
- Linux arm64 (Ubuntu ARM)
- macOS x64 (Intel)
- macOS arm64 (Apple Silicon)
- Windows x64

#### 9.2 Cross-Compile Script
- `scripts/build-all-platforms.sh`:
  - Build native image per platform
  - Output to `bin/clicker-java-{os}-{arch}`

**Note:** GraalVM native-image doesn't support cross-compilation from a single machine. Each platform must be built on its native OS/arch, typically via CI/CD (GitHub Actions).

---

### Phase 10: MCP Server (Future)

#### 10.1 MCP Protocol Handler
- Implement `McpServer.java`:
  - Read JSON-RPC 2.0 from stdin
  - Write JSON-RPC 2.0 to stdout
  - Handle: initialize, tools/list, tools/call

#### 10.2 Tool Handlers
- Implement `McpHandlers.java`:
  - browser_launch
  - browser_navigate
  - browser_click
  - browser_type
  - browser_screenshot
  - browser_find
  - browser_quit

---

## GraalVM Native Image Considerations

### Challenges & Solutions

| Challenge | Solution |
|-----------|----------|
| **Reflection** | Generate reflect-config.json using native-image agent |
| **Dynamic class loading** | Avoid or pre-register classes |
| **Resources** | Declare in resource-config.json |
| **Network libs** | Use libraries with native-image support |
| **Process spawning** | Use ProcessBuilder (GraalVM compatible) |
| **Signal handling** | Use sun.misc.Signal (supported in native-image) |

### Native Image Agent Workflow
```bash
# Run with agent to generate config
java -agentlib:native-image-agent=config-output-dir=src/main/resources/META-INF/native-image \
     -jar target/clicker-java.jar navigate https://example.com

# Build native image using generated config
mvn package -Pnative
```

---

## Build Commands

### Development (JVM)
```bash
mvn compile exec:java -Dexec.mainClass="com.vibium.clicker.Main" -Dexec.args="--version"
```

### Native Build (Current Platform)
```bash
mvn package -Pnative
./target/clicker-java --version
```

### Native Build (All Platforms via CI)
```yaml
# GitHub Actions matrix
strategy:
  matrix:
    include:
      - os: ubuntu-latest
        arch: amd64
      - os: ubuntu-24.04-arm64
        arch: arm64
      - os: macos-13
        arch: x64
      - os: macos-14
        arch: arm64
      - os: windows-latest
        arch: x64
```

---

## pom.xml Outline

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.vibium</groupId>
    <artifactId>clicker-java</artifactId>
    <version>0.1.0</version>
    <packaging>jar</packaging>

    <properties>
        <java.version>21</java.version>
        <maven.compiler.source>21</maven.compiler.source>
        <maven.compiler.target>21</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>

        <picocli.version>4.7.6</picocli.version>
        <jackson.version>2.17.0</jackson.version>
        <java-websocket.version>1.5.6</java-websocket.version>
        <graalvm.version>24.0.0</graalvm.version>
    </properties>

    <dependencies>
        <!-- CLI Framework -->
        <dependency>
            <groupId>info.picocli</groupId>
            <artifactId>picocli</artifactId>
            <version>${picocli.version}</version>
        </dependency>

        <!-- JSON -->
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-databind</artifactId>
            <version>${jackson.version}</version>
        </dependency>

        <!-- WebSocket -->
        <dependency>
            <groupId>org.java-websocket</groupId>
            <artifactId>Java-WebSocket</artifactId>
            <version>${java-websocket.version}</version>
        </dependency>

        <!-- Logging -->
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-api</artifactId>
            <version>2.0.12</version>
        </dependency>
        <dependency>
            <groupId>ch.qos.logback</groupId>
            <artifactId>logback-classic</artifactId>
            <version>1.5.3</version>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <!-- Picocli annotation processor -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.12.1</version>
                <configuration>
                    <annotationProcessorPaths>
                        <path>
                            <groupId>info.picocli</groupId>
                            <artifactId>picocli-codegen</artifactId>
                            <version>${picocli.version}</version>
                        </path>
                    </annotationProcessorPaths>
                </configuration>
            </plugin>

            <!-- Fat JAR -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-shade-plugin</artifactId>
                <version>3.5.2</version>
                <executions>
                    <execution>
                        <phase>package</phase>
                        <goals>
                            <goal>shade</goal>
                        </goals>
                        <configuration>
                            <transformers>
                                <transformer implementation="org.apache.maven.plugins.shade.resource.ManifestResourceTransformer">
                                    <mainClass>com.vibium.clicker.Main</mainClass>
                                </transformer>
                            </transformers>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>

    <profiles>
        <profile>
            <id>native</id>
            <build>
                <plugins>
                    <plugin>
                        <groupId>org.graalvm.buildtools</groupId>
                        <artifactId>native-maven-plugin</artifactId>
                        <version>0.10.1</version>
                        <extensions>true</extensions>
                        <executions>
                            <execution>
                                <id>build-native</id>
                                <goals>
                                    <goal>compile-no-fork</goal>
                                </goals>
                                <phase>package</phase>
                            </execution>
                        </executions>
                        <configuration>
                            <imageName>clicker-java</imageName>
                            <mainClass>com.vibium.clicker.Main</mainClass>
                            <buildArgs>
                                <buildArg>--no-fallback</buildArg>
                                <buildArg>-H:+ReportExceptionStackTraces</buildArg>
                            </buildArgs>
                        </configuration>
                    </plugin>
                </plugins>
            </build>
        </profile>
    </profiles>
</project>
```

---

## Feature Parity Checklist

| Feature | Go Clicker | Java Clicker |
|---------|------------|--------------|
| CLI Framework | Cobra | Picocli |
| WebSocket Client | gorilla/websocket | Java-WebSocket |
| WebSocket Server | gorilla/websocket | Java-WebSocket |
| JSON Parsing | encoding/json | Jackson |
| HTTP Client | net/http | java.net.http |
| Process Management | os/exec | ProcessBuilder |
| Signal Handling | os/signal | sun.misc.Signal |
| File Operations | os | java.nio.file |
| ZIP Extraction | archive/zip | java.util.zip |

---

## Estimated Effort

| Phase | Description | Complexity |
|-------|-------------|------------|
| 1 | Project Setup & CLI | Low |
| 2 | Paths & Installation | Medium |
| 3 | WebSocket & BiDi Types | Medium |
| 4 | Browser Launching | High |
| 5 | Navigation & Screenshots | Medium |
| 6 | Elements & Input | Medium |
| 7 | Proxy Server | High |
| 8 | Native Image Config | Medium |
| 9 | Cross-Platform Builds | Low (CI/CD) |
| 10 | MCP Server | Medium |

---

## Success Criteria

1. **Functional Parity**: All Go clicker commands work identically in Java version
2. **Startup Time**: Native binary starts in <50ms
3. **Binary Size**: Target <30MB (comparable to Go binary)
4. **Memory Usage**: <100MB during typical operation
5. **Cross-Platform**: Works on Linux, macOS, Windows (x64 and arm64)
6. **No Runtime Dependencies**: Single static binary, no JVM needed

---

## Next Steps

1. Create Maven project structure
2. Implement CLI framework with Picocli
3. Port platform paths and browser installer
4. Implement BiDi protocol layer
5. Add browser launching and process management
6. Build and test native image
7. Add proxy server and remaining commands

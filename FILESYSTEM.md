# Vibium V1 File System Layout

> **Note:** This document is actively maintained until V1 ships, then archived as a historical reference.

```
vibium/
│
├── README.md
├── LICENSE
├── CLAUDE.md                             # Claude Code instructions
├── CONTRIBUTING.md                       # Development guide
├── Makefile                              # Build & test targets
├── V1-ROADMAP.md
├── V2-ROADMAP.md
├── FILESYSTEM.md
├── .gitignore
├── package.json                          # npm workspaces root
├── package-lock.json
│
├── clicker/                              # Go Core Engine
│   ├── go.mod
│   ├── go.sum
│   │
│   ├── cmd/
│   │   └── clicker/
│   │       └── main.go                   # CLI entry point (cobra)
│   │
│   ├── internal/
│   │   ├── bidi/                         # WebDriver BiDi Protocol
│   │   │   ├── connection.go             # WebSocket connection (gorilla)
│   │   │   ├── protocol.go               # BiDi message types
│   │   │   ├── session.go                # session module
│   │   │   ├── browsingcontext.go        # browsingContext module
│   │   │   ├── script.go                 # script module
│   │   │   ├── input.go                  # input module
│   │   │   └── element.go                # Element finding via script
│   │   │
│   │   ├── proxy/                        # BiDi Pass-through Proxy
│   │   │   ├── server.go                 # WebSocket proxy server
│   │   │   └── router.go                 # Message routing (goroutines)
│   │   │
│   │   ├── browser/                      # Browser Management
│   │   │   ├── launcher.go               # Browser process launching
│   │   │   └── installer.go              # Chrome for Testing downloader
│   │   │
│   │   ├── features/                     # High-level Features
│   │   │   ├── actionability.go          # Actionability checks
│   │   │   └── autowait.go               # Auto-waiting logic
│   │   │
│   │   ├── mcp/                          # MCP Server Interface
│   │   │   ├── server.go                 # MCP server (stdio JSON-RPC)
│   │   │   ├── handlers.go               # MCP tool handlers
│   │   │   └── schema.go                 # Tool schemas
│   │   │
│   │   ├── paths/
│   │   │   └── paths.go                  # Platform-specific paths
│   │   │
│   │   └── process/
│   │       └── process.go                # Process management
│   │
│   └── bin/                              # Build output (gitignored)
│       └── clicker
│
├── clients/
│   │
│   ├── javascript/                       # JS/TS Client Library
│   │   ├── package.json
│   │   ├── tsconfig.json
│   │   ├── tsup.config.ts                # Build config
│   │   │
│   │   └── src/
│   │       ├── index.ts                  # Main exports
│   │       ├── browser.ts                # browser.launch() entry (async)
│   │       ├── vibe.ts                   # Vibe class (async API)
│   │       ├── element.ts                # Element class (async)
│   │       │
│   │       ├── sync/                     # Sync API Wrappers
│   │       │   ├── index.ts              # Sync exports
│   │       │   ├── bridge.ts             # Worker thread bridge (Atomics.wait)
│   │       │   ├── worker.ts             # Worker thread (runs async ops)
│   │       │   ├── browser.ts            # browserSync.launch()
│   │       │   ├── vibe.ts               # VibeSync class
│   │       │   └── element.ts            # ElementSync class
│   │       │
│   │       ├── clicker/                  # Clicker Binary Management
│   │       │   ├── binary.ts             # Binary path resolution
│   │       │   ├── process.ts            # Spawn & manage clicker
│   │       │   └── platform.ts           # Platform detection
│   │       │
│   │       ├── bidi/                     # BiDi Client
│   │       │   ├── connection.ts         # WebSocket connection
│   │       │   ├── client.ts             # BiDi command client
│   │       │   └── types.ts              # BiDi type definitions
│   │       │
│   │       └── utils/
│   │           ├── errors.ts             # Error classes
│   │           └── debug.ts              # Debug logging
│   │
│   └── java/                             # Java Client Library
│       ├── pom.xml                       # Maven build (generates fat JAR)
│       │
│       ├── src/main/java/com/vibium/
│       │   ├── Browser.java              # Browser.launch() entry point
│       │   ├── Vibe.java                 # Vibe class (go, screenshot, quit)
│       │   ├── LaunchOptions.java        # Launch configuration (builder)
│       │   │
│       │   ├── bidi/                     # BiDi Protocol Layer
│       │   │   ├── BiDiConnection.java   # WebSocket client
│       │   │   ├── BiDiClient.java       # Command/response handling
│       │   │   ├── BiDiCommand.java      # Command record
│       │   │   ├── BiDiResponse.java     # Response record
│       │   │   ├── BiDiEvent.java        # Event record
│       │   │   ├── BiDiError.java        # Error record
│       │   │   └── types/
│       │   │       ├── BrowsingContextInfo.java
│       │   │       ├── BrowsingContextTree.java
│       │   │       ├── NavigationResult.java
│       │   │       └── ScreenshotResult.java
│       │   │
│       │   ├── clicker/                  # Clicker Binary Management
│       │   │   ├── BinaryResolver.java   # Binary path resolution
│       │   │   ├── ClickerProcess.java   # Spawn & manage clicker
│       │   │   └── Platform.java         # OS/arch detection
│       │   │
│       │   └── exceptions/
│       │       ├── VibiumException.java
│       │       ├── ConnectionException.java
│       │       └── TimeoutException.java
│       │
│       ├── src/test/java/com/vibium/
│       │   └── BrowserTest.java          # Integration tests
│       │
│       ├── examples/
│       │   └── Screenshot.java           # Example usage
│       │
│       └── target/                       # Build output (gitignored)
│           ├── vibium-*-all.jar          # Fat JAR for Katalon Studio
│           ├── vibium-*.jar              # Slim JAR
│           └── vibium-*-sources.jar      # Sources JAR
│
├── tests/                                # Test suites
│   ├── TODO.md
│   ├── cli/                              # CLI tests
│   │   ├── navigation.test.js
│   │   ├── elements.test.js
│   │   ├── actionability.test.js
│   │   └── process.test.js
│   ├── js/                               # JS library tests
│   │   ├── async-api.test.js
│   │   ├── sync-api.test.js
│   │   ├── auto-wait.test.js
│   │   ├── browser-modes.test.js
│   │   └── process.test.js
│   └── mcp/                              # MCP server tests
│       └── server.test.js
│
├── docs/
│   ├── local-dev-setup-mac.md            # VM development setup (macOS)
│   ├── local-dev-setup-x86-linux.md      # VM development setup (x86 Linux)
│   ├── local-dev-setup-x86-windows.md    # VM development setup (x86 Windows)
│   ├── explanation/
│   │   ├── actionability.md              # How actionability works
│   │   └── process-cleanup.md            # Process management docs
│   ├── reference/
│   │   └── WebDriver-Bidi-Spec.md        # BiDi protocol reference
│   ├── tutorials/
│   │   └── claude-code-mcp-setup.md      # MCP setup guide
│   └── updates/                          # Development updates
│       ├── 2025-12-11-v1-announcement.md
│       ├── 2025-12-16-week1-progress.md
│       ├── 2025-12-17-halfway-there.md
│       ├── 2025-12-19-day8-elements-sync.md
│       ├── 2025-12-19-day9-actionability.md
│       └── 2025-12-20-day10-mcp.md
│
├── packages/                             # Platform-specific npm packages (Day 12-13)
│   ├── vibium/                           # Main package: npm vibium
│   │   ├── package.json
│   │   ├── index.js                      # Re-exports clients/javascript
│   │   ├── bin.js                        # npx entry → execs clicker mcp
│   │   └── postinstall.js                # Runs clicker install for Chrome
│   ├── linux-x64/                        # npm: @vibium/linux-x64
│   │   ├── package.json
│   │   └── bin/clicker
│   ├── linux-arm64/                      # npm: @vibium/linux-arm64
│   │   ├── package.json
│   │   └── bin/clicker
│   ├── darwin-x64/                       # npm: @vibium/darwin-x64
│   │   ├── package.json
│   │   └── bin/clicker
│   ├── darwin-arm64/                     # npm: @vibium/darwin-arm64
│   │   ├── package.json
│   │   └── bin/clicker
│   └── win32-x64/                        # npm: @vibium/win32-x64
│       ├── package.json
│       └── bin/clicker.exe
│
├── examples/                             # Example projects (Day 14)
│   ├── async-basic/
│   │   ├── package.json
│   │   └── index.ts
│   ├── sync-basic/
│   │   ├── package.json
│   │   └── index.ts
│   └── claude-code-mcp/
│       └── README.md
│
└── scripts/                              # Build scripts (Day 12)
    └── package-npm.sh                    # Package for npm publish
```

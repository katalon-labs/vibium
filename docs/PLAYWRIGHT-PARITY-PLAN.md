# Vibium-Playwright Parity Plan

A roadmap for bringing Vibium to feature parity with Playwright, organized by timeline.

---

## Current State Comparison

### What Vibium Has (V1)

| Category | Feature | Status |
|----------|---------|--------|
| **Browser Support** | Chrome/Chromium | ✅ |
| **Browser Support** | Firefox | ❌ |
| **Browser Support** | WebKit/Safari | ❌ |
| **Protocol** | WebDriver BiDi | ✅ |
| **Language Clients** | JavaScript/TypeScript | ✅ |
| **Language Clients** | Python | ❌ |
| **Language Clients** | Java/.NET | ❌ |
| **Navigation** | goto/navigate | ✅ |
| **Navigation** | Wait for load states | ⚠️ Partial |
| **Navigation** | History (back/forward) | ❌ |
| **Selectors** | CSS selectors | ✅ |
| **Selectors** | XPath | ❌ |
| **Selectors** | Text selectors | ❌ |
| **Selectors** | getByRole/getByLabel/getByTestId | ❌ |
| **Selectors** | Shadow DOM piercing | ❌ |
| **Element Actions** | click() | ✅ |
| **Element Actions** | type()/fill() | ✅ |
| **Element Actions** | getAttribute() | ✅ |
| **Element Actions** | textContent() | ✅ |
| **Element Actions** | hover() | ❌ |
| **Element Actions** | selectOption() | ❌ |
| **Element Actions** | check()/uncheck() | ❌ |
| **Element Actions** | dragTo() | ❌ |
| **Element Actions** | scrollIntoView() | ❌ |
| **Element Actions** | focus()/blur() | ❌ |
| **Element Actions** | press() (keyboard) | ❌ |
| **Actionability** | Visible check | ✅ |
| **Actionability** | Stable check | ✅ |
| **Actionability** | Receives events check | ✅ |
| **Actionability** | Enabled check | ✅ |
| **Actionability** | Editable check | ✅ |
| **Auto-Wait** | Wait for element existence | ✅ |
| **Auto-Wait** | Wait for actionability | ✅ |
| **Auto-Wait** | Auto-retry assertions | ❌ |
| **Screenshots** | Viewport screenshot | ✅ |
| **Screenshots** | Full page screenshot | ❌ |
| **Screenshots** | Element screenshot | ❌ |
| **Evaluation** | evaluate() | ✅ |
| **Evaluation** | evaluateHandle() | ❌ |
| **Evaluation** | expose functions | ❌ |
| **Browser Contexts** | Single context | ✅ |
| **Browser Contexts** | Multiple contexts | ❌ |
| **Browser Contexts** | Incognito contexts | ❌ |
| **Pages/Tabs** | Single page | ✅ |
| **Pages/Tabs** | Multiple pages/tabs | ❌ |
| **Pages/Tabs** | Popup handling | ❌ |
| **Frames** | Frame navigation | ❌ |
| **Frames** | iframe interaction | ❌ |
| **Dialogs** | Alert/confirm/prompt handling | ❌ |
| **File Operations** | File upload | ❌ |
| **File Operations** | File download | ❌ |
| **Network** | Request interception | ❌ |
| **Network** | Response mocking | ❌ |
| **Network** | HAR recording | ❌ |
| **Network** | Network events | ❌ |
| **Video** | Video recording | ❌ |
| **Tracing** | Trace recording | ❌ |
| **Tracing** | Trace viewer | ❌ |
| **Testing** | Test runner | ❌ |
| **Testing** | Assertions library | ❌ |
| **Testing** | Test fixtures | ❌ |
| **Testing** | Parallelization | ❌ |
| **Testing** | Retries | ❌ |
| **Emulation** | Viewport emulation | ❌ |
| **Emulation** | Device emulation | ❌ |
| **Emulation** | Geolocation | ❌ |
| **Emulation** | Timezone | ❌ |
| **Emulation** | Permissions | ❌ |
| **Dev Tools** | Codegen (record & playback) | ❌ |
| **Dev Tools** | Inspector | ❌ |
| **AI/MCP** | MCP server integration | ✅ |
| **AI/MCP** | AI-powered locators | ❌ |

### Summary Metrics

| Metric | Vibium V1 | Playwright |
|--------|-----------|------------|
| Core actions implemented | ~8 | ~50+ |
| Browser engines | 1 | 3 |
| Language bindings | 1 | 5 |
| Selector strategies | 1 | 8+ |
| Test framework features | 0 | Full suite |

---

## Parity Roadmap

### 🗓️ One Week (7 days)

**Focus: Essential element actions and selectors**

| Priority | Feature | Effort | Impact |
|----------|---------|--------|--------|
| P0 | `hover()` action | 1 day | High - common action |
| P0 | `selectOption()` for dropdowns | 1 day | High - form automation |
| P0 | `check()`/`uncheck()` for checkboxes | 0.5 day | High - form automation |
| P0 | `press()` for keyboard shortcuts | 0.5 day | High - keyboard nav |
| P1 | XPath selector support | 1 day | Medium - legacy compat |
| P1 | `scrollIntoView()` | 0.5 day | Medium - visibility |
| P1 | `focus()`/`blur()` | 0.5 day | Medium - forms |
| P2 | Text selector (`text=`) | 1 day | Medium - convenience |

**Deliverables:**
- [ ] Go: Add hover, selectOption, check/uncheck, press, scrollIntoView, focus/blur
- [ ] Go: XPath selector parsing and execution
- [ ] Go: Text selector implementation
- [ ] JS Client: Expose new element methods
- [ ] MCP: Add new tools for hover, select, check
- [ ] Tests: Coverage for all new actions

**Verification:**
- Can automate a complete form with dropdowns, checkboxes, and keyboard shortcuts
- XPath selectors work for legacy test suites

---

### 🗓️ One Month (30 days)

**Focus: Multi-page, frames, network, and Python client**

#### Week 1-2: Browser Context & Multi-Page

| Priority | Feature | Effort | Impact |
|----------|---------|--------|--------|
| P0 | Multiple browser contexts | 2 days | High - test isolation |
| P0 | Multiple pages/tabs support | 2 days | High - real-world flows |
| P0 | Popup/new window handling | 1 day | High - auth flows |
| P1 | Frame/iframe navigation | 2 days | Medium - embedded content |
| P1 | Dialog handling (alert/confirm/prompt) | 1 day | Medium - UX testing |
| P2 | Full page screenshot | 0.5 day | Medium - debugging |
| P2 | Element screenshot | 0.5 day | Medium - visual testing |

#### Week 3: Network Layer

| Priority | Feature | Effort | Impact |
|----------|---------|--------|--------|
| P0 | Network event monitoring | 2 days | High - API testing |
| P0 | Request interception | 2 days | High - mocking |
| P1 | Response mocking | 1 day | High - test isolation |
| P2 | HAR recording/export | 1 day | Medium - debugging |

#### Week 4: Python Client

| Priority | Feature | Effort | Impact |
|----------|---------|--------|--------|
| P0 | Python sync API | 3 days | High - ecosystem reach |
| P0 | Python async API | 2 days | High - async frameworks |
| P1 | PyPI packaging | 1 day | Medium - distribution |
| P1 | Python documentation | 1 day | Medium - adoption |

**Deliverables:**
- [ ] Go: BrowserContext manager with multiple contexts
- [ ] Go: Page manager with tab/popup handling
- [ ] Go: Frame traversal and interaction
- [ ] Go: BiDi network module integration
- [ ] Go: Request/response interception
- [ ] Python: `vibium` package with sync/async APIs
- [ ] Python: PyPI release
- [ ] JS Client: Multi-page and network APIs
- [ ] MCP: Network-related tools
- [ ] Docs: Python getting started guide

**Verification:**
- Can run isolated tests in separate browser contexts
- Can handle OAuth popup flows
- Can mock API responses for testing
- `pip install vibium` works and runs basic automation

---

### 🗓️ One Quarter (90 days)

**Focus: Testing framework, tracing, emulation, and Firefox**

#### Month 1 (covered above)

#### Month 2: Testing Framework & Tracing

| Priority | Feature | Effort | Impact |
|----------|---------|--------|--------|
| P0 | Assertion library (expect-style) | 1 week | High - test ergonomics |
| P0 | Auto-retrying assertions | 3 days | High - flaky test reduction |
| P0 | Soft assertions | 2 days | Medium - debugging |
| P1 | Test fixtures system | 1 week | High - test organization |
| P1 | Parallel test execution | 1 week | High - speed |
| P1 | Test retry on failure | 2 days | Medium - flaky tests |
| P0 | Trace recording | 1 week | High - debugging |
| P1 | Trace viewer (web UI) | 1 week | High - debugging |
| P2 | Video recording | 1 week | Medium - debugging |

#### Month 3: Emulation & Firefox

| Priority | Feature | Effort | Impact |
|----------|---------|--------|--------|
| P0 | Viewport emulation | 2 days | High - responsive testing |
| P0 | Device emulation presets | 2 days | High - mobile testing |
| P1 | Geolocation spoofing | 1 day | Medium - location apps |
| P1 | Timezone emulation | 1 day | Medium - intl apps |
| P1 | Permission handling | 2 days | Medium - notifications, camera |
| P0 | Firefox support | 1 week | High - cross-browser |
| P2 | Codegen (record & playback) | 2 weeks | Medium - onboarding |

**Deliverables:**
- [ ] Vibium Test: New `@vibium/test` package
- [ ] Vibium Test: expect() with auto-retry
- [ ] Vibium Test: Fixture system (page, context, browser)
- [ ] Vibium Test: Parallel execution with worker isolation
- [ ] Vibium Test: Configurable retries
- [ ] Go: Trace recording with DOM snapshots
- [ ] Web: Trace viewer application
- [ ] Go: Video recording via FFmpeg
- [ ] Go: Emulation APIs (viewport, device, geo, timezone)
- [ ] Go: GeckoDriver integration for Firefox
- [ ] CLI: `vibium codegen` command

**Verification:**
- Can write tests with `expect(locator).toHaveText('...')` that auto-retry
- Tests run in parallel with full isolation
- Can view trace files to debug failures
- Can test responsive designs across device presets
- Tests pass on both Chrome and Firefox

---

### 🗓️ One Year (365 days)

**Focus: Full ecosystem parity, enterprise features, and innovation**

#### Q1 (covered above)

#### Q2: WebKit, Java, Advanced Selectors

| Priority | Feature | Effort | Impact |
|----------|---------|--------|--------|
| P0 | WebKit/Safari support | 2 weeks | High - Apple ecosystem |
| P0 | Java client | 2 weeks | High - enterprise |
| P0 | .NET client | 2 weeks | Medium - enterprise |
| P0 | getByRole() selector | 1 week | High - accessibility |
| P0 | getByLabel() selector | 3 days | High - form testing |
| P0 | getByTestId() selector | 2 days | High - recommended pattern |
| P0 | getByPlaceholder() selector | 2 days | Medium - forms |
| P1 | Shadow DOM piercing | 1 week | Medium - web components |
| P1 | Strict mode (single match) | 3 days | Medium - reliability |

#### Q3: Developer Experience & Tooling

| Priority | Feature | Effort | Impact |
|----------|---------|--------|--------|
| P0 | Playwright Inspector equivalent | 3 weeks | High - debugging |
| P0 | VS Code extension | 2 weeks | High - IDE integration |
| P1 | UI mode (interactive test runner) | 3 weeks | Medium - exploration |
| P1 | Accessibility testing APIs | 2 weeks | Medium - a11y |
| P1 | Clock/time manipulation | 1 week | Medium - time-dependent tests |
| P2 | Storage state persistence | 1 week | Medium - auth reuse |
| P2 | API testing integration | 2 weeks | Medium - full-stack |

#### Q4: AI & Cloud Features (Vibium Differentiation)

| Priority | Feature | Effort | Impact |
|----------|---------|--------|--------|
| P0 | AI-powered locators (`vibe.do()`) | 4 weeks | Very High - USP |
| P0 | Natural language assertions | 2 weeks | Very High - USP |
| P0 | Self-healing selectors | 3 weeks | High - maintenance |
| P1 | Cortex (persistent app memory) | 3 weeks | High - agent memory |
| P1 | Retina (session recording) | 2 weeks | Medium - debugging |
| P1 | Cloud execution service | 4 weeks | High - scaling |
| P2 | Visual regression testing | 3 weeks | Medium - UI testing |
| P2 | Component testing mode | 2 weeks | Medium - unit testing |

**End of Year Deliverables:**
- [ ] Full browser parity: Chrome, Firefox, WebKit
- [ ] Full language parity: JS, Python, Java, .NET
- [ ] Full selector parity: CSS, XPath, text, role, label, testid
- [ ] Full testing parity: assertions, fixtures, parallel, retries, tracing
- [ ] Full tooling parity: inspector, codegen, VS Code extension
- [ ] AI differentiation: natural language actions and assertions
- [ ] Cloud offering: managed execution service

**Verification:**
- Can migrate any Playwright test suite to Vibium with minimal changes
- AI features provide 10x productivity improvement over manual selectors
- Enterprise customers can use Java/.NET clients in CI/CD pipelines

---

## Feature Priority Matrix

| Feature Category | Week 1 | Month 1 | Quarter 1 | Year 1 |
|------------------|--------|---------|-----------|--------|
| Element Actions | 🟢 Core | 🟢 Complete | - | - |
| Selectors | 🟡 Basic | 🟡 Extended | 🟢 Full | - |
| Multi-Page | - | 🟢 Core | 🟢 Complete | - |
| Network | - | 🟢 Core | 🟢 Complete | - |
| Python | - | 🟢 Complete | - | - |
| Testing Framework | - | - | 🟢 Core | 🟢 Complete |
| Tracing/Video | - | - | 🟢 Core | 🟢 Complete |
| Firefox | - | - | 🟢 Complete | - |
| WebKit | - | - | - | 🟢 Complete |
| Java/.NET | - | - | - | 🟢 Complete |
| Dev Tools | - | - | 🟡 Basic | 🟢 Complete |
| AI Features | - | - | - | 🟢 Core |
| Cloud | - | - | - | 🟡 Beta |

---

## Risk Assessment

| Risk | Likelihood | Impact | Mitigation |
|------|------------|--------|------------|
| BiDi spec gaps in Firefox/WebKit | Medium | High | Fallback to CDP where needed |
| AI locators accuracy issues | High | Medium | Hybrid approach: AI + deterministic fallback |
| Test framework complexity | Medium | Medium | Start minimal, iterate based on feedback |
| Python/Java client maintenance burden | Medium | Medium | Code generation from Go types |
| Performance at scale | Low | High | Benchmark early, optimize hot paths |

---

## Success Metrics

| Timeframe | Metric | Target |
|-----------|--------|--------|
| Week 1 | Element actions coverage | 90% of common actions |
| Month 1 | Python weekly downloads | 1,000+ |
| Quarter 1 | GitHub stars | 5,000+ |
| Quarter 1 | Test framework adoption | 100+ companies |
| Year 1 | Playwright migration stories | 10+ published |
| Year 1 | Enterprise customers | 5+ |

---

## Resources

### Playwright Reference
- [Locator API](https://playwright.dev/docs/api/class-locator)
- [Page API](https://playwright.dev/docs/api/class-page)
- [Browser API](https://playwright.dev/docs/api/class-browser)
- [Network](https://playwright.dev/docs/network)
- [Tracing](https://playwright.dev/docs/api/class-tracing)
- [Assertions](https://playwright.dev/docs/test-assertions)
- [Auto-waiting](https://playwright.dev/docs/actionability)
- [Best Practices](https://playwright.dev/docs/best-practices)

### Vibium Reference
- [V1-ROADMAP.md](../V1-ROADMAP.md)
- [V2-ROADMAP.md](../V2-ROADMAP.md)
- [FILESYSTEM.md](../FILESYSTEM.md)

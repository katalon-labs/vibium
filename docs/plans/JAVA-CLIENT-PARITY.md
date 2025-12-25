# Java Client Parity Plan

This document outlines the gaps between the Java client and the JS client, with a plan to bring them to feature parity.

## Summary

The Java client (`clients/java`) provides basic browser automation but is missing key features that the JS client has, particularly around **auto-wait/actionability checks** and the **vibium: custom commands**.

## Current State

### What Java Client Has
- `Browser.launch(options)` - Launch browser with headless/port/executablePath options
- `Vibe.go(url)` - Navigate to URL
- `Vibe.screenshot()` - Take screenshot (returns byte[])
- `Vibe.find(selector)` - Find element (but uses raw script.callFunction)
- `Vibe.quit()` / `close()` - Cleanup
- `Element.click()` - Click element (but uses raw input.performActions)
- `Element.type(text)` - Type text (but uses raw input.performActions)
- `Element.text()` - Get text content
- `Element.getAttribute(name)` - Get attribute value
- `Element.boundingBox()` - Get element bounds
- Exception classes: VibiumException, ConnectionException, TimeoutException

### What Java Client is Missing

| Feature | JS Client | Java Client | Priority |
|---------|-----------|-------------|----------|
| Actionability checks on click | `vibium:click` | raw `input.performActions` | **Critical** |
| Actionability checks on type | `vibium:type` | raw `input.performActions` | **Critical** |
| Auto-wait on find | `vibium:find` with timeout | raw `script.callFunction` | **Critical** |
| evaluate(script) | ✓ | ✗ | High |
| Headless default = false | ✓ | Defaults to true | High |
| find() timeout option | `find(selector, {timeout})` | `find(selector)` only | High |
| click/type timeout option | `click({timeout})` | `click()` only | Medium |
| ElementNotFoundError | ✓ | Uses generic VibiumException | Medium |
| BrowserCrashedError | ✓ | Missing | Medium |

## Critical Issues

### 1. No Actionability Checks

The biggest gap is that the Java client's `click()` and `type()` methods use raw BiDi `input.performActions` commands:

```java
// Java client - Element.click() - NO actionability checks!
client.send("input.performActions", params);
```

While the JS client uses the custom `vibium:click` command that the clicker binary implements with full actionability checks:

```typescript
// JS client - Element.click() - Has actionability checks
await this.client.send('vibium:click', {
  context: this.context,
  selector: this.selector,
  timeout: options?.timeout,
});
```

The actionability checks ensure the element is:
- Visible in viewport
- Stable (not animating)
- Enabled
- Receiving pointer events
- (for type) Editable

### 2. No Auto-Wait on Find

Similarly, Java's `find()` uses raw `script.callFunction` which returns immediately if the element doesn't exist:

```java
// Java - returns null immediately if not found
ScriptResult result = client.send("script.callFunction", params, ScriptResult.class);
```

While JS uses `vibium:find` which waits for the element to appear:

```typescript
// JS - waits up to timeout for element to exist
const result = await this.client.send<VibiumFindResult>('vibium:find', {
  context,
  selector,
  timeout: options?.timeout,
});
```

## Implementation Plan

### Phase 1: Critical Fixes (Actionability & Auto-Wait)

**Goal:** Make Java client use the same `vibium:*` commands as JS client

1. **Update `Vibe.find()` to use `vibium:find`**
   - Add `FindOptions` class with `timeout` field
   - Change from `script.callFunction` to `vibium:find`
   - File: `clients/java/src/main/java/com/vibium/Vibe.java`

2. **Update `Element.click()` to use `vibium:click`**
   - Add `ActionOptions` class with `timeout` field
   - Change from `input.performActions` to `vibium:click`
   - File: `clients/java/src/main/java/com/vibium/Element.java`

3. **Update `Element.type()` to use `vibium:type`**
   - Use `ActionOptions` for timeout
   - Change from `input.performActions` to `vibium:type`
   - File: `clients/java/src/main/java/com/vibium/Element.java`

### Phase 2: API Completeness

4. **Add `Vibe.evaluate(script)` method**
   - Execute arbitrary JavaScript in page context
   - Return deserialized result
   - File: `clients/java/src/main/java/com/vibium/Vibe.java`

5. **Fix headless default to `false`**
   - Match JS client's visible-by-default behavior
   - File: `clients/java/src/main/java/com/vibium/LaunchOptions.java`

### Phase 3: Error Handling

6. **Add `ElementNotFoundException`**
   - New exception class extending VibiumException
   - File: `clients/java/src/main/java/com/vibium/exceptions/ElementNotFoundException.java`

7. **Add `BrowserCrashedException`**
   - Include exit code and optional output
   - File: `clients/java/src/main/java/com/vibium/exceptions/BrowserCrashedException.java`

8. **Update code to throw specific exceptions**
   - Use ElementNotFoundException in find/text/boundingBox
   - Use BrowserCrashedException in ClickerProcess

### Phase 4: Tests & Documentation

9. **Update tests to verify actionability**
   - Test that click waits for visibility
   - Test that type waits for editability
   - Test find timeout behavior

10. **Update examples**
    - Show timeout usage
    - Show error handling patterns

## Files to Modify

```
clients/java/
├── src/main/java/com/vibium/
│   ├── Vibe.java              # Add evaluate(), update find()
│   ├── Element.java           # Update click(), type()
│   ├── LaunchOptions.java     # Fix headless default
│   ├── FindOptions.java       # NEW - timeout option for find
│   ├── ActionOptions.java     # NEW - timeout option for actions
│   └── exceptions/
│       ├── ElementNotFoundException.java  # NEW
│       └── BrowserCrashedException.java   # NEW
└── src/test/java/com/vibium/
    └── BrowserTest.java       # Update tests
```

## Validation

After implementation, the following should work identically in both clients:

```java
// Java
Vibe vibe = Browser.launch();  // Opens visible browser (not headless)
vibe.go("https://example.com");

Element button = vibe.find("button.submit", new FindOptions().timeout(5000));
// ^ Waits up to 5s for button to appear

button.click();
// ^ Waits for button to be visible, stable, enabled, receiving events

Element input = vibe.find("input[name='email']");
input.type("test@example.com");
// ^ Waits for input to be editable before typing

String result = vibe.evaluate("return document.title");
```

```typescript
// JS equivalent
const vibe = await browser.launch();  // Opens visible browser
await vibe.go("https://example.com");

const button = await vibe.find("button.submit", { timeout: 5000 });
await button.click();

const input = await vibe.find("input[name='email']");
await input.type("test@example.com");

const result = await vibe.evaluate("return document.title");
```

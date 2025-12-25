# Java Client Parity Plan

This document outlines the gaps between the Java client and the JS client, with a plan to bring them to feature parity.

## Status: ✅ IMPLEMENTED

All critical and high-priority items have been implemented. The Java client now uses the same `vibium:*` commands as the JS client for full actionability checks and auto-wait support.

## What Was Implemented

### Phase 1: Critical Fixes (Actionability & Auto-Wait) ✅

1. **`Vibe.find()` now uses `vibium:find`** ✅
   - Added `FindOptions` class with `timeout` field
   - Changed from `script.callFunction` to `vibium:find`
   - Auto-waits for element to appear

2. **`Element.click()` now uses `vibium:click`** ✅
   - Added `ActionOptions` class with `timeout` field
   - Changed from `input.performActions` to `vibium:click`
   - Full actionability checks (visible, stable, enabled, receives events)

3. **`Element.type()` now uses `vibium:type`** ✅
   - Uses `ActionOptions` for timeout
   - Changed from `input.performActions` to `vibium:type`
   - Full actionability checks including editable

### Phase 2: API Completeness ✅

4. **Added `Vibe.evaluate(script)` method** ✅
   - Execute arbitrary JavaScript in page context
   - Returns deserialized result

5. **Fixed headless default to `false`** ✅
   - Now matches JS client's visible-by-default behavior

### Phase 3: Error Handling ✅

6. **Added `ElementNotFoundException`** ✅
   - Used in `text()` and `boundingBox()` methods

7. **Added `BrowserCrashedException`** ✅
   - Available for use in ClickerProcess

### Bonus: Element.find() ✅

8. **Added `Element.find(selector)` for nested element search** ✅
   - Mirrors JS client's element.find() capability

## Files Added/Modified

```
clients/java/src/main/java/com/vibium/
├── Vibe.java                    # Updated: find(), added evaluate()
├── Element.java                 # Updated: click(), type(), added find()
├── LaunchOptions.java           # Updated: headless default = false
├── FindOptions.java             # NEW: timeout option for find
├── ActionOptions.java           # NEW: timeout option for actions
├── bidi/types/
│   └── VibiumFindResult.java    # NEW: result type for vibium:find
└── exceptions/
    ├── ElementNotFoundException.java  # NEW
    └── BrowserCrashedException.java   # NEW
```

## API Parity

The following now works identically in both clients:

```java
// Java
try (Vibe vibe = Browser.launch()) {  // Opens visible browser (not headless)
    vibe.go("https://example.com");

    Element button = vibe.find("button.submit", new FindOptions().timeout(5000));
    // ^ Waits up to 5s for button to appear

    button.click();
    // ^ Waits for button to be visible, stable, enabled, receiving events

    Element input = vibe.find("input[name='email']");
    input.type("test@example.com");
    // ^ Waits for input to be editable before typing

    String title = vibe.evaluate("return document.title");
}
```

```typescript
// JS equivalent
const vibe = await browser.launch();  // Opens visible browser
await vibe.go("https://example.com");

const button = await vibe.find("button.submit", { timeout: 5000 });
await button.click();

const input = await vibe.find("input[name='email']");
await input.type("test@example.com");

const title = await vibe.evaluate("return document.title");
await vibe.quit();
```

## Remaining Differences

The Java client still has a few minor differences that don't affect functionality:

1. **Sync vs Async**: Java API is synchronous (blocking), JS has both sync and async
2. **Debug logging**: JS has debug utility, Java doesn't have equivalent yet
3. **Sync wrapper pattern**: JS uses worker threads for sync API, Java is natively sync

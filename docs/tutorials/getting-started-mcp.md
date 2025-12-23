# Getting Started with Vibium MCP

Let AI control your browser. This guide shows you how to add Vibium to Claude Code so Claude can browse the web for you.

> **Note:** This guide covers Claude Code. Support for more language models and MCP clients is coming soon.

---

## What You'll Get

After setup, you can ask Claude things like:
- "Take a screenshot of https://example.com"
- "Go to Hacker News and find the top story"
- "Fill out this form and click submit"

Claude will control a real browser to do it.

---

## Prerequisites

You need Claude Code installed. Check if you have it:

```bash
claude --version
```

If you don't have it, install from [claude.ai/code](https://claude.ai/code).

---

## Step 1: Add Vibium to Claude Code

One command:

```bash
claude mcp add vibium -- npx -y vibium
```

That's it. Chrome downloads automatically during setup.

---

## Step 2: Restart Claude Code

Close and reopen Claude Code (or restart your terminal session).

---

## Step 3: Try It

Start a conversation with Claude and ask:

```
Take a screenshot of https://example.com
```

You'll see:
1. A Chrome window open
2. The page load
3. Claude respond with the screenshot

---

## Available Commands

Once Vibium is added, Claude can use these tools:

| Tool | What It Does |
|------|--------------|
| `browser_launch` | Opens Chrome (visible by default) |
| `browser_navigate` | Goes to a URL |
| `browser_find` | Finds an element by CSS selector |
| `browser_click` | Clicks an element |
| `browser_type` | Types text into an element |
| `browser_screenshot` | Captures the page |
| `browser_quit` | Closes the browser |

You don't need to know these — just ask Claude what you want in plain English.

---

## Example Prompts

**Simple screenshot:**
```
Take a screenshot of https://news.ycombinator.com
```

**Navigate and interact:**
```
Go to https://example.com and click the "Learn more" link
```

**Fill a form:**
```
Go to https://httpbin.org/forms/post and fill out the form with test data
```

**Research task:**
```
Go to Wikipedia and find out when the Eiffel Tower was built
```

---

## Running Headless

By default, you'll see the browser window (great for watching what Claude does). To run invisibly:

Ask Claude: "Take a screenshot of example.com using headless mode"

Or modify the MCP config to always run headless — but visible mode is recommended for debugging.

---

## Troubleshooting

### "MCP server not found"

Make sure you ran the add command:
```bash
claude mcp add vibium -- npx -y vibium
```

Then restart Claude Code.

### Browser doesn't open

Check that Chrome downloaded successfully:
```bash
npx vibium
```

This should start the MCP server. Press Ctrl+C to stop.

### "Permission denied" (Linux)

Install Chrome dependencies:
```bash
sudo apt-get install -y libgbm1 libnss3 libatk-bridge2.0-0 libdrm2 libxkbcommon0 libxcomposite1 libxdamage1 libxfixes3 libxrandr2 libasound2
```

### Check MCP status

```bash
claude mcp list
```

You should see `vibium` in the list.

---

## Removing Vibium

If you want to remove it:

```bash
claude mcp remove vibium
```

---

## Next Steps

**Use the JS API directly:**
See [Getting Started Tutorial](getting-started.md) for programmatic control.

**Learn more about MCP:**
[Model Context Protocol docs](https://modelcontextprotocol.io)

---

## You Did It! 🎉

Claude can now browse the web. Use it for:
- Research tasks
- Web scraping
- Testing websites
- Automating repetitive browsing

Questions? [Open an issue](https://github.com/VibiumDev/vibium/issues).

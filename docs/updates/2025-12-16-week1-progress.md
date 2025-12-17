# week 1 progress

we're building. here's where we're at.

---

## what's working

the go binary (`clicker`) can now:

- **auto-install chrome for testing + chromedriver** (no manual setup)
- **launch chrome** via chromedriver with webdriver bidi enabled
- **navigate** to any url
- **take screenshots** (headless or headed)
- **find elements** by css selector
- **click** elements
- **type** into inputs

all the core browser primitives are in place. the foundation is solid.

---

## human review checkpoint #1: passed

just finished the first human review checkpoint. tested everything manually:

- chrome launches and exits cleanly
- no zombie processes (even on ctrl+c mid-operation)
- screenshots render correctly
- click navigates pages
- keyboard input works on real sites

tested on example.com, the-internet.herokuapp.com, and vibium.com. everything works.

---

## cli in action

```bash
# take a screenshot
clicker screenshot https://example.com -o shot.png

# find an element
clicker find https://example.com "a"
# → tag=A, text="Learn more", box={x:151, y:151, w:82, h:18}

# click a link
clicker click https://example.com "a"
# → navigates to iana.org

# type into an input
clicker type https://the-internet.herokuapp.com/inputs "input" "12345"
```

flags for debugging:
- `--headed` shows the browser window
- `--wait-open 5` waits for page load
- `--wait-close 3` keeps browser visible before closing

---

## building with claude code

it's been a joy using claude code to follow the v1 roadmap. each milestone is a prompt. claude reads the roadmap, implements the feature, runs the checkpoint test, and we move on.

bootstrapping everything as a go-based command line utility has made testing dead simple. no web server to spin up, no frontend to click through. just `go build` and run the command. instant feedback loop.

the roadmap has human review checkpoints baked in — moments to step back, test manually, and make sure things actually work before moving on. passed the first one today. 

this workflow is exactly what vibium is about: ai and humans working together on real software.

---

## what's next

- **day 6**: bidi proxy server (websocket server that routes messages between clients and chrome)
- **day 7-8**: javascript/typescript client with async and sync apis
- **day 9**: auto-wait for elements
- **day 10**: mcp server for claude code integration

we're on track for christmas.

---

*december 16, 2025*

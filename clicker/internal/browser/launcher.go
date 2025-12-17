package browser

import (
	"bytes"
	"encoding/json"
	"fmt"
	"io"
	"net"
	"net/http"
	"os"
	"os/exec"
	"syscall"
	"time"

	"github.com/vibium/clicker/internal/paths"
	"github.com/vibium/clicker/internal/process"
)

// prefixWriter wraps an io.Writer and prepends a prefix to each line.
type prefixWriter struct {
	w      io.Writer
	prefix string
	atBOL  bool // at beginning of line
}

func newPrefixWriter(w io.Writer, prefix string) *prefixWriter {
	return &prefixWriter{w: w, prefix: prefix, atBOL: true}
}

func (pw *prefixWriter) Write(p []byte) (n int, err error) {
	for _, b := range p {
		if pw.atBOL {
			if _, err := pw.w.Write([]byte(pw.prefix)); err != nil {
				return n, err
			}
			pw.atBOL = false
		}
		if _, err := pw.w.Write([]byte{b}); err != nil {
			return n, err
		}
		n++
		if b == '\n' {
			pw.atBOL = true
		}
	}
	return n, nil
}

// LaunchOptions contains options for launching the browser.
type LaunchOptions struct {
	Headless bool
	Port     int  // Chromedriver port, 0 = auto-select
	Verbose  bool // Show chromedriver output
}

// LaunchResult contains the result of launching the browser via chromedriver.
type LaunchResult struct {
	WebSocketURL   string
	SessionID      string
	ChromedriverCmd *exec.Cmd
	Port           int
}

// sessionRequest is the payload for creating a new session.
type sessionRequest struct {
	Capabilities capabilities `json:"capabilities"`
}

type capabilities struct {
	AlwaysMatch alwaysMatch `json:"alwaysMatch"`
}

type alwaysMatch struct {
	BrowserName  string   `json:"browserName"`
	WebSocketURL bool     `json:"webSocketUrl"`
	Args         []string `json:"goog:chromeOptions,omitempty"`
}

type chromeOptions struct {
	Args   []string `json:"args,omitempty"`
	Binary string   `json:"binary,omitempty"`
}

// sessionResponse is the response from creating a new session.
type sessionResponse struct {
	Value sessionValue `json:"value"`
}

type sessionValue struct {
	SessionID    string                 `json:"sessionId"`
	Capabilities map[string]interface{} `json:"capabilities"`
}

// Launch starts chromedriver and creates a BiDi session.
func Launch(opts LaunchOptions) (*LaunchResult, error) {
	chromedriverPath, err := paths.GetChromedriverPath()
	if err != nil {
		return nil, fmt.Errorf("chromedriver not found: %w (run 'clicker install' first)", err)
	}

	chromePath, err := paths.GetChromeExecutable()
	if err != nil {
		return nil, fmt.Errorf("Chrome not found: %w (run 'clicker install' first)", err)
	}

	// Find available port
	port := opts.Port
	if port == 0 {
		port, err = findAvailablePort()
		if err != nil {
			return nil, fmt.Errorf("failed to find available port: %w", err)
		}
	}

	// Start chromedriver as a process group leader so we can kill all children
	cmd := exec.Command(chromedriverPath, fmt.Sprintf("--port=%d", port))
	cmd.SysProcAttr = &syscall.SysProcAttr{Setpgid: true}
	if opts.Verbose {
		fmt.Println("       ------- chromedriver -------")
		pw := newPrefixWriter(os.Stdout, "       ")
		cmd.Stdout = pw
		cmd.Stderr = pw
	}
	if err := cmd.Start(); err != nil {
		return nil, fmt.Errorf("failed to start chromedriver: %w", err)
	}

	// Track for cleanup
	process.Track(cmd)

	// Wait for chromedriver to be ready
	baseURL := fmt.Sprintf("http://localhost:%d", port)
	if err := waitForChromedriver(baseURL, 10*time.Second); err != nil {
		cmd.Process.Kill()
		return nil, fmt.Errorf("chromedriver failed to start: %w", err)
	}

	if opts.Verbose {
		fmt.Println("       ----------------------------")
	}

	// Create session with BiDi enabled
	sessionID, wsURL, err := createSession(baseURL, chromePath, opts.Headless, opts.Verbose)
	if err != nil {
		cmd.Process.Kill()
		return nil, fmt.Errorf("failed to create session: %w", err)
	}

	return &LaunchResult{
		WebSocketURL:    wsURL,
		SessionID:       sessionID,
		ChromedriverCmd: cmd,
		Port:            port,
	}, nil
}

// findAvailablePort finds an available TCP port.
func findAvailablePort() (int, error) {
	listener, err := net.Listen("tcp", "127.0.0.1:0")
	if err != nil {
		return 0, err
	}
	defer listener.Close()
	return listener.Addr().(*net.TCPAddr).Port, nil
}

// waitForChromedriver waits for chromedriver to be ready.
func waitForChromedriver(baseURL string, timeout time.Duration) error {
	deadline := time.Now().Add(timeout)
	for time.Now().Before(deadline) {
		resp, err := http.Get(baseURL + "/status")
		if err == nil {
			resp.Body.Close()
			if resp.StatusCode == http.StatusOK {
				return nil
			}
		}
		time.Sleep(100 * time.Millisecond)
	}
	return fmt.Errorf("timeout waiting for chromedriver")
}

// createSession creates a new WebDriver session with BiDi enabled.
func createSession(baseURL, chromePath string, headless, verbose bool) (string, string, error) {
	args := []string{
		"--no-sandbox",
		"--no-first-run",
		"--no-default-browser-check",
		"--disable-infobars",
		"--disable-blink-features=AutomationControlled",
		"--disable-crash-reporter",
		"--disable-background-networking",
		"--disable-background-timer-throttling",
		"--disable-backgrounding-occluded-windows",
		"--disable-breakpad",
		"--disable-component-extensions-with-background-pages",
		"--disable-component-update",
		"--disable-default-apps",
		"--disable-dev-shm-usage",
		"--disable-extensions",
		"--disable-features=TranslateUI",
		"--disable-hang-monitor",
		"--disable-ipc-flooding-protection",
		"--disable-popup-blocking",
		"--disable-prompt-on-repost",
		"--disable-renderer-backgrounding",
		"--disable-sync",
		"--enable-features=NetworkService,NetworkServiceInProcess",
		"--force-color-profile=srgb",
		"--metrics-recording-only",
		"--password-store=basic",
		"--use-mock-keychain",
	}

	if headless {
		args = append(args, "--headless=new")
	}

	reqBody := map[string]interface{}{
		"capabilities": map[string]interface{}{
			"alwaysMatch": map[string]interface{}{
				"browserName":  "chrome",
				"webSocketUrl": true,
				"goog:chromeOptions": map[string]interface{}{
					"binary":          chromePath,
					"args":            args,
					"excludeSwitches": []string{"enable-automation"},
				},
			},
		},
	}

	jsonBody, err := json.Marshal(reqBody)
	if err != nil {
		return "", "", err
	}

	if verbose {
		fmt.Println("       ------- POST /session -------")
		fmt.Printf("       --> %s\n", string(jsonBody))
	}

	resp, err := http.Post(baseURL+"/session", "application/json", bytes.NewReader(jsonBody))
	if err != nil {
		return "", "", err
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK && resp.StatusCode != http.StatusCreated {
		return "", "", fmt.Errorf("failed to create session: HTTP %d", resp.StatusCode)
	}

	// Read response body for logging and parsing
	respBody, err := io.ReadAll(resp.Body)
	if err != nil {
		return "", "", fmt.Errorf("failed to read session response: %w", err)
	}

	if verbose {
		fmt.Printf("       <-- %s\n", string(respBody))
		fmt.Println("       ------------------------------")
	}

	var sessResp sessionResponse
	if err := json.Unmarshal(respBody, &sessResp); err != nil {
		return "", "", fmt.Errorf("failed to decode session response: %w", err)
	}

	wsURL, ok := sessResp.Value.Capabilities["webSocketUrl"].(string)
	if !ok || wsURL == "" {
		return "", "", fmt.Errorf("webSocketUrl not found in session capabilities")
	}

	return sessResp.Value.SessionID, wsURL, nil
}

// Close terminates a chromedriver session and process.
func (r *LaunchResult) Close() error {
	// Delete session first (tells chromedriver to quit Chrome gracefully)
	if r.SessionID != "" && r.Port > 0 {
		req, _ := http.NewRequest(http.MethodDelete, fmt.Sprintf("http://localhost:%d/session/%s", r.Port, r.SessionID), nil)
		if req != nil {
			http.DefaultClient.Do(req)
		}
		// Give Chrome a moment to quit gracefully
		time.Sleep(100 * time.Millisecond)
	}

	// Kill the entire process group (chromedriver + all child processes including Chrome)
	if r.ChromedriverCmd != nil && r.ChromedriverCmd.Process != nil {
		pgid, err := syscall.Getpgid(r.ChromedriverCmd.Process.Pid)
		if err == nil {
			syscall.Kill(-pgid, syscall.SIGKILL)
		} else {
			// Fallback to killing just the process
			r.ChromedriverCmd.Process.Kill()
		}
		process.Untrack(r.ChromedriverCmd)
	}

	return nil
}

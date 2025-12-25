package com.vibium;

import com.google.gson.Gson;
import com.vibium.bidi.BiDiClient;
import com.vibium.bidi.types.BrowsingContextTree;
import com.vibium.bidi.types.ElementInfo;
import com.vibium.bidi.types.NavigationResult;
import com.vibium.bidi.types.ScreenshotResult;
import com.vibium.bidi.types.ScriptResult;
import com.vibium.bidi.types.VibiumFindResult;
import com.vibium.clicker.ClickerProcess;
import com.vibium.exceptions.VibiumException;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Main browser automation interface.
 * Mirrors clients/javascript/src/vibe.ts
 */
public class Vibe implements AutoCloseable {
    private static final Gson gson = new Gson();

    private final BiDiClient client;
    private final ClickerProcess process;
    private String context;

    Vibe(BiDiClient client, ClickerProcess process) {
        this.client = client;
        this.process = process;
    }

    /**
     * Get the browsing context ID, lazily initializing if needed.
     */
    private String getContext() {
        if (context != null) {
            return context;
        }

        BrowsingContextTree tree = client.send(
            "browsingContext.getTree",
            new HashMap<>(),
            BrowsingContextTree.class
        );

        if (tree.contexts() == null || tree.contexts().isEmpty()) {
            throw new VibiumException("No browsing context available");
        }

        context = tree.contexts().get(0).context();
        return context;
    }

    /**
     * Navigate to a URL.
     *
     * @param url The URL to navigate to
     */
    public void go(String url) {
        Map<String, Object> params = new HashMap<>();
        params.put("context", getContext());
        params.put("url", url);
        params.put("wait", "complete");

        client.send("browsingContext.navigate", params, NavigationResult.class);
    }

    /**
     * Take a screenshot of the current page.
     *
     * @return The screenshot as a PNG byte array
     */
    public byte[] screenshot() {
        Map<String, Object> params = new HashMap<>();
        params.put("context", getContext());

        ScreenshotResult result = client.send(
            "browsingContext.captureScreenshot",
            params,
            ScreenshotResult.class
        );

        return Base64.getDecoder().decode(result.data());
    }

    /**
     * Execute JavaScript in the page context.
     *
     * @param script The JavaScript code to execute
     * @return The result of the script execution
     */
    @SuppressWarnings("unchecked")
    public <T> T evaluate(String script) {
        String ctx = getContext();

        Map<String, Object> params = new HashMap<>();
        params.put("functionDeclaration", "() => { " + script + " }");
        params.put("target", Map.of("context", ctx));
        params.put("arguments", java.util.List.of());
        params.put("awaitPromise", true);
        params.put("resultOwnership", "root");

        ScriptResult result = client.send("script.callFunction", params, ScriptResult.class);

        if (result.result() != null && result.result().value() != null) {
            return (T) result.result().value();
        }
        return null;
    }

    /**
     * Find an element by CSS selector with default timeout.
     * Waits for element to exist before returning.
     *
     * @param selector The CSS selector
     * @return The element
     * @throws VibiumException if the element is not found within timeout
     */
    public Element find(String selector) {
        return find(selector, new FindOptions());
    }

    /**
     * Find an element by CSS selector.
     * Waits for element to exist before returning.
     *
     * @param selector The CSS selector
     * @param options Find options (timeout, etc.)
     * @return The element
     * @throws VibiumException if the element is not found within timeout
     */
    public Element find(String selector, FindOptions options) {
        String ctx = getContext();

        Map<String, Object> params = new HashMap<>();
        params.put("context", ctx);
        params.put("selector", selector);
        params.put("timeout", options.getTimeout());

        VibiumFindResult result = client.send("vibium:find", params, VibiumFindResult.class);

        ElementInfo info = new ElementInfo(result.tag(), result.text(), result.box());
        return new Element(client, ctx, selector, info);
    }

    /**
     * Close the browser and cleanup resources.
     */
    public void quit() {
        close();
    }

    /**
     * Close the browser and cleanup resources.
     * Implements AutoCloseable for try-with-resources.
     */
    @Override
    public void close() {
        client.close();
        if (process != null) {
            process.stop();
        }
    }
}

package com.vibium;

import com.google.gson.Gson;
import com.vibium.bidi.BiDiClient;
import com.vibium.bidi.types.BoundingBox;
import com.vibium.bidi.types.BrowsingContextTree;
import com.vibium.bidi.types.ElementInfo;
import com.vibium.bidi.types.NavigationResult;
import com.vibium.bidi.types.ScreenshotResult;
import com.vibium.bidi.types.ScriptResult;
import com.vibium.clicker.ClickerProcess;
import com.vibium.exceptions.VibiumException;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
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
     * Find an element by CSS selector.
     *
     * @param selector The CSS selector
     * @return The element
     * @throws VibiumException if the element is not found
     */
    public Element find(String selector) {
        String ctx = getContext();

        Map<String, Object> params = new HashMap<>();
        params.put("functionDeclaration", """
            (selector) => {
                const el = document.querySelector(selector);
                if (!el) return null;
                const rect = el.getBoundingClientRect();
                return JSON.stringify({
                    tag: el.tagName,
                    text: (el.textContent || '').trim().substring(0, 100),
                    box: {
                        x: rect.x,
                        y: rect.y,
                        width: rect.width,
                        height: rect.height
                    }
                });
            }""");
        params.put("target", Map.of("context", ctx));
        params.put("arguments", List.of(Map.of("type", "string", "value", selector)));
        params.put("awaitPromise", false);
        params.put("resultOwnership", "root");

        ScriptResult result = client.send("script.callFunction", params, ScriptResult.class);

        if ("null".equals(result.result().type())) {
            throw new VibiumException("Element not found: " + selector);
        }

        ElementInfo info = gson.fromJson(result.result().value().toString(), ElementInfo.class);
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

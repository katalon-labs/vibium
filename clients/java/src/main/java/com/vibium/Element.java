package com.vibium;

import com.google.gson.Gson;
import com.vibium.bidi.BiDiClient;
import com.vibium.bidi.types.BoundingBox;
import com.vibium.bidi.types.ElementInfo;
import com.vibium.bidi.types.ScriptResult;
import com.vibium.exceptions.ElementNotFoundException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents an element on the page.
 * Mirrors clients/javascript/src/element.ts
 */
public class Element {
    private static final Gson gson = new Gson();

    private final BiDiClient client;
    private final String context;
    private final String selector;
    private final ElementInfo info;

    Element(BiDiClient client, String context, String selector, ElementInfo info) {
        this.client = client;
        this.context = context;
        this.selector = selector;
        this.info = info;
    }

    /**
     * Click the element with default timeout.
     * Waits for element to be visible, stable, receive events, and enabled.
     */
    public void click() {
        click(new ActionOptions());
    }

    /**
     * Click the element.
     * Waits for element to be visible, stable, receive events, and enabled.
     *
     * @param options Action options (timeout, etc.)
     */
    public void click(ActionOptions options) {
        Map<String, Object> params = new HashMap<>();
        params.put("context", context);
        params.put("selector", selector);
        params.put("timeout", options.getTimeout());

        client.send("vibium:click", params);
    }

    /**
     * Type text into the element with default timeout.
     * Waits for element to be visible, stable, receive events, enabled, and editable.
     *
     * @param text The text to type
     */
    public void type(String text) {
        type(text, new ActionOptions());
    }

    /**
     * Type text into the element.
     * Waits for element to be visible, stable, receive events, enabled, and editable.
     *
     * @param text The text to type
     * @param options Action options (timeout, etc.)
     */
    public void type(String text, ActionOptions options) {
        Map<String, Object> params = new HashMap<>();
        params.put("context", context);
        params.put("selector", selector);
        params.put("text", text);
        params.put("timeout", options.getTimeout());

        client.send("vibium:type", params);
    }

    /**
     * Find a child element by CSS selector.
     * Waits for element to exist before returning.
     *
     * @param selector The CSS selector (relative to this element)
     * @return The child element
     */
    public Element find(String selector) {
        return find(selector, new FindOptions());
    }

    /**
     * Find a child element by CSS selector.
     * Waits for element to exist before returning.
     *
     * @param selector The CSS selector (relative to this element)
     * @param options Find options (timeout, etc.)
     * @return The child element
     */
    public Element find(String childSelector, FindOptions options) {
        // Combine parent and child selectors
        String combinedSelector = this.selector + " " + childSelector;

        Map<String, Object> params = new HashMap<>();
        params.put("context", context);
        params.put("selector", combinedSelector);
        params.put("timeout", options.getTimeout());

        var result = client.send("vibium:find", params,
            com.vibium.bidi.types.VibiumFindResult.class);

        ElementInfo childInfo = new ElementInfo(result.tag(), result.text(), result.box());
        return new Element(client, context, combinedSelector, childInfo);
    }

    /**
     * Get the text content of the element.
     *
     * @return The text content
     */
    public String text() {
        Map<String, Object> params = new HashMap<>();
        params.put("functionDeclaration", """
            (selector) => {
                const el = document.querySelector(selector);
                return el ? (el.textContent || '').trim() : null;
            }""");
        params.put("target", Map.of("context", context));
        params.put("arguments", List.of(Map.of("type", "string", "value", selector)));
        params.put("awaitPromise", false);
        params.put("resultOwnership", "root");

        ScriptResult result = client.send("script.callFunction", params, ScriptResult.class);

        if ("null".equals(result.result().type())) {
            throw new ElementNotFoundException(selector);
        }

        return result.result().value().toString();
    }

    /**
     * Get an attribute value from the element.
     *
     * @param name The attribute name
     * @return The attribute value, or null if not found
     */
    public String getAttribute(String name) {
        Map<String, Object> params = new HashMap<>();
        params.put("functionDeclaration", """
            (selector, attrName) => {
                const el = document.querySelector(selector);
                return el ? el.getAttribute(attrName) : null;
            }""");
        params.put("target", Map.of("context", context));
        params.put("arguments", List.of(
            Map.of("type", "string", "value", selector),
            Map.of("type", "string", "value", name)
        ));
        params.put("awaitPromise", false);
        params.put("resultOwnership", "root");

        ScriptResult result = client.send("script.callFunction", params, ScriptResult.class);

        if ("null".equals(result.result().type())) {
            return null;
        }

        return result.result().value().toString();
    }

    /**
     * Get the bounding box of the element.
     *
     * @return The bounding box
     */
    public BoundingBox boundingBox() {
        Map<String, Object> params = new HashMap<>();
        params.put("functionDeclaration", """
            (selector) => {
                const el = document.querySelector(selector);
                if (!el) return null;
                const rect = el.getBoundingClientRect();
                return JSON.stringify({
                    x: rect.x,
                    y: rect.y,
                    width: rect.width,
                    height: rect.height
                });
            }""");
        params.put("target", Map.of("context", context));
        params.put("arguments", List.of(Map.of("type", "string", "value", selector)));
        params.put("awaitPromise", false);
        params.put("resultOwnership", "root");

        ScriptResult result = client.send("script.callFunction", params, ScriptResult.class);

        if ("null".equals(result.result().type())) {
            throw new ElementNotFoundException(selector);
        }

        return gson.fromJson(result.result().value().toString(), BoundingBox.class);
    }

    /**
     * Get the selector used to find this element.
     *
     * @return The CSS selector
     */
    public String getSelector() {
        return selector;
    }

    /**
     * Get the cached element info.
     *
     * @return The element info
     */
    public ElementInfo getInfo() {
        return info;
    }
}

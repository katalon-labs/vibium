package com.vibium;

import com.google.gson.Gson;
import com.vibium.bidi.BiDiClient;
import com.vibium.bidi.types.BoundingBox;
import com.vibium.bidi.types.ElementInfo;
import com.vibium.bidi.types.ScriptResult;
import com.vibium.exceptions.VibiumException;

import java.util.ArrayList;
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
     * Click the element.
     */
    public void click() {
        double[] center = getCenter();
        int x = (int) Math.round(center[0]);
        int y = (int) Math.round(center[1]);

        List<Map<String, Object>> pointerActions = new ArrayList<>();
        pointerActions.add(Map.of(
            "type", "pointerMove",
            "x", x,
            "y", y,
            "duration", 0
        ));
        pointerActions.add(Map.of(
            "type", "pointerDown",
            "button", 0
        ));
        pointerActions.add(Map.of(
            "type", "pointerUp",
            "button", 0
        ));

        List<Map<String, Object>> actions = new ArrayList<>();
        Map<String, Object> pointerAction = new HashMap<>();
        pointerAction.put("type", "pointer");
        pointerAction.put("id", "mouse");
        pointerAction.put("parameters", Map.of("pointerType", "mouse"));
        pointerAction.put("actions", pointerActions);
        actions.add(pointerAction);

        Map<String, Object> params = new HashMap<>();
        params.put("context", context);
        params.put("actions", actions);

        client.send("input.performActions", params);
    }

    /**
     * Type text into the element.
     *
     * @param text The text to type
     */
    public void type(String text) {
        // Click to focus first
        click();

        // Build key actions for each character
        List<Map<String, Object>> keyActions = new ArrayList<>();
        for (char c : text.toCharArray()) {
            keyActions.add(Map.of("type", "keyDown", "value", String.valueOf(c)));
            keyActions.add(Map.of("type", "keyUp", "value", String.valueOf(c)));
        }

        List<Map<String, Object>> actions = new ArrayList<>();
        Map<String, Object> keyAction = new HashMap<>();
        keyAction.put("type", "key");
        keyAction.put("id", "keyboard");
        keyAction.put("actions", keyActions);
        actions.add(keyAction);

        Map<String, Object> params = new HashMap<>();
        params.put("context", context);
        params.put("actions", actions);

        client.send("input.performActions", params);
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
            throw new VibiumException("Element not found: " + selector);
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
            throw new VibiumException("Element not found: " + selector);
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

    private double[] getCenter() {
        return new double[] {
            info.box().x() + info.box().width() / 2,
            info.box().y() + info.box().height() / 2
        };
    }
}

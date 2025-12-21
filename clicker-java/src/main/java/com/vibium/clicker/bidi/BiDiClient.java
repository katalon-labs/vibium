package com.vibium.clicker.bidi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vibium.clicker.bidi.BiDiProtocol.*;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class BiDiClient {

    private final BiDiConnection connection;
    private final ObjectMapper objectMapper;
    private boolean verbose = false;

    public BiDiClient(BiDiConnection connection) {
        this.connection = connection;
        this.objectMapper = new ObjectMapper();
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
        this.connection.setVerbose(verbose);
    }

    public void close() throws InterruptedException {
        connection.closeBlocking();
    }

    public Response sendCommand(String method, Map<String, Object> params) throws Exception {
        Command cmd = new Command(method, params != null ? params : new HashMap<>());
        String json = objectMapper.writeValueAsString(cmd);

        connection.sendMessage(json);

        // Wait for response with matching ID
        while (true) {
            String responseJson = connection.receiveMessage(30, TimeUnit.SECONDS);
            if (responseJson == null) {
                throw new Exception("Timeout waiting for response to " + method);
            }

            Response response = objectMapper.readValue(responseJson, Response.class);

            // Skip events
            if (response.isEvent()) {
                if (verbose) {
                    System.err.println("[BiDi] Event: " + response.method);
                }
                continue;
            }

            // Check for matching ID
            if (response.id != null && response.id == cmd.id) {
                if (response.isError()) {
                    throw new Exception("BiDi error: " + response.error);
                }
                return response;
            }
        }
    }

    // Session commands

    public SessionStatus sessionStatus() throws Exception {
        Response response = sendCommand("session.status", null);
        return objectMapper.treeToValue(response.result, SessionStatus.class);
    }

    // BrowsingContext commands

    public BrowsingContextInfo[] getTree() throws Exception {
        Response response = sendCommand("browsingContext.getTree", null);
        JsonNode contexts = response.result.get("contexts");
        return objectMapper.treeToValue(contexts, BrowsingContextInfo[].class);
    }

    public String getFirstContext() throws Exception {
        BrowsingContextInfo[] contexts = getTree();
        if (contexts == null || contexts.length == 0) {
            throw new Exception("No browsing contexts available");
        }
        return contexts[0].context;
    }

    public NavigateResult navigate(String context, String url) throws Exception {
        if (context == null || context.isEmpty()) {
            context = getFirstContext();
        }

        Map<String, Object> params = new HashMap<>();
        params.put("context", context);
        params.put("url", url);
        params.put("wait", "complete");

        Response response = sendCommand("browsingContext.navigate", params);
        return objectMapper.treeToValue(response.result, NavigateResult.class);
    }

    public String getCurrentUrl(String context) throws Exception {
        if (context == null || context.isEmpty()) {
            BrowsingContextInfo[] contexts = getTree();
            if (contexts != null && contexts.length > 0) {
                return contexts[0].url;
            }
        }
        return null;
    }

    public byte[] captureScreenshot(String context) throws Exception {
        if (context == null || context.isEmpty()) {
            context = getFirstContext();
        }

        Map<String, Object> params = new HashMap<>();
        params.put("context", context);

        Response response = sendCommand("browsingContext.captureScreenshot", params);
        String base64Data = response.result.get("data").asText();
        return Base64.getDecoder().decode(base64Data);
    }

    // Script commands

    public RealmInfo[] getRealms(String context) throws Exception {
        Map<String, Object> params = new HashMap<>();
        if (context != null && !context.isEmpty()) {
            params.put("context", context);
        }

        Response response = sendCommand("script.getRealms", params);
        JsonNode realms = response.result.get("realms");
        return objectMapper.treeToValue(realms, RealmInfo[].class);
    }

    public EvaluateResult evaluate(String context, String expression) throws Exception {
        if (context == null || context.isEmpty()) {
            context = getFirstContext();
        }

        Map<String, Object> params = new HashMap<>();
        params.put("expression", expression);
        params.put("target", Map.of("context", context));
        params.put("awaitPromise", true);

        Response response = sendCommand("script.evaluate", params);
        return objectMapper.treeToValue(response.result, EvaluateResult.class);
    }

    public EvaluateResult callFunction(String context, String functionDeclaration, List<Object> args) throws Exception {
        if (context == null || context.isEmpty()) {
            context = getFirstContext();
        }

        Map<String, Object> params = new HashMap<>();
        params.put("functionDeclaration", functionDeclaration);
        params.put("target", Map.of("context", context));
        params.put("awaitPromise", true);
        params.put("arguments", args != null ? args : List.of());

        Response response = sendCommand("script.callFunction", params);
        return objectMapper.treeToValue(response.result, EvaluateResult.class);
    }

    // Element commands

    public ElementInfo findElement(String context, String selector) throws Exception {
        String js = """
            (selector) => {
                const el = document.querySelector(selector);
                if (!el) return null;
                const rect = el.getBoundingClientRect();
                return {
                    tagName: el.tagName,
                    textContent: (el.textContent || '').substring(0, 100).trim(),
                    x: rect.x,
                    y: rect.y,
                    width: rect.width,
                    height: rect.height
                };
            }
            """;

        List<Object> args = List.of(Map.of("type", "string", "value", selector));
        EvaluateResult result = callFunction(context, js, args);

        if (result.result == null || result.result.value == null) {
            return null;
        }

        Object value = result.result.value;
        if (value instanceof Map<?, ?> map) {
            ElementInfo info = new ElementInfo();
            info.tagName = String.valueOf(map.get("tagName"));
            info.textContent = String.valueOf(map.get("textContent"));
            info.x = ((Number) map.get("x")).doubleValue();
            info.y = ((Number) map.get("y")).doubleValue();
            info.width = ((Number) map.get("width")).doubleValue();
            info.height = ((Number) map.get("height")).doubleValue();
            return info;
        }

        return null;
    }

    public String getElementValue(String context, String selector) throws Exception {
        String js = """
            (selector) => {
                const el = document.querySelector(selector);
                return el ? el.value : null;
            }
            """;

        List<Object> args = List.of(Map.of("type", "string", "value", selector));
        EvaluateResult result = callFunction(context, js, args);

        if (result.result != null && result.result.value != null) {
            return result.result.value.toString();
        }
        return null;
    }

    // Input commands

    public void performActions(String context, List<Map<String, Object>> actions) throws Exception {
        if (context == null || context.isEmpty()) {
            context = getFirstContext();
        }

        Map<String, Object> params = new HashMap<>();
        params.put("context", context);
        params.put("actions", actions);

        sendCommand("input.performActions", params);
    }

    public void click(String context, double x, double y) throws Exception {
        List<Map<String, Object>> actions = List.of(
            Map.of(
                "type", "pointer",
                "id", "mouse",
                "parameters", Map.of("pointerType", "mouse"),
                "actions", List.of(
                    Map.of("type", "pointerMove", "x", (int) x, "y", (int) y),
                    Map.of("type", "pointerDown", "button", 0),
                    Map.of("type", "pointerUp", "button", 0)
                )
            )
        );

        performActions(context, actions);
    }

    public void clickElement(String context, String selector) throws Exception {
        ElementInfo element = findElement(context, selector);
        if (element == null) {
            throw new Exception("Element not found: " + selector);
        }

        double centerX = element.x + element.width / 2;
        double centerY = element.y + element.height / 2;
        click(context, centerX, centerY);
    }

    public void doubleClick(String context, double x, double y) throws Exception {
        List<Map<String, Object>> actions = List.of(
            Map.of(
                "type", "pointer",
                "id", "mouse",
                "parameters", Map.of("pointerType", "mouse"),
                "actions", List.of(
                    Map.of("type", "pointerMove", "x", (int) x, "y", (int) y),
                    Map.of("type", "pointerDown", "button", 0),
                    Map.of("type", "pointerUp", "button", 0),
                    Map.of("type", "pointerDown", "button", 0),
                    Map.of("type", "pointerUp", "button", 0)
                )
            )
        );

        performActions(context, actions);
    }

    public void typeText(String context, String text) throws Exception {
        if (context == null || context.isEmpty()) {
            context = getFirstContext();
        }

        List<Map<String, Object>> keyActions = new java.util.ArrayList<>();
        for (char c : text.toCharArray()) {
            keyActions.add(Map.of("type", "keyDown", "value", String.valueOf(c)));
            keyActions.add(Map.of("type", "keyUp", "value", String.valueOf(c)));
        }

        List<Map<String, Object>> actions = List.of(
            Map.of(
                "type", "key",
                "id", "keyboard",
                "actions", keyActions
            )
        );

        performActions(context, actions);
    }

    public void typeIntoElement(String context, String selector, String text) throws Exception {
        clickElement(context, selector);
        Thread.sleep(100); // Small delay for focus
        typeText(context, text);
    }

    public void pressKey(String context, String key) throws Exception {
        if (context == null || context.isEmpty()) {
            context = getFirstContext();
        }

        List<Map<String, Object>> actions = List.of(
            Map.of(
                "type", "key",
                "id", "keyboard",
                "actions", List.of(
                    Map.of("type", "keyDown", "value", key),
                    Map.of("type", "keyUp", "value", key)
                )
            )
        );

        performActions(context, actions);
    }

    public void moveMouse(String context, double x, double y) throws Exception {
        List<Map<String, Object>> actions = List.of(
            Map.of(
                "type", "pointer",
                "id", "mouse",
                "parameters", Map.of("pointerType", "mouse"),
                "actions", List.of(
                    Map.of("type", "pointerMove", "x", (int) x, "y", (int) y)
                )
            )
        );

        performActions(context, actions);
    }
}

package com.vibium.clicker.bidi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class BiDiProtocol {

    private static final AtomicLong idGenerator = new AtomicLong(0);

    public static long nextId() {
        return idGenerator.incrementAndGet();
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Command {
        @JsonProperty("id")
        public long id;

        @JsonProperty("method")
        public String method;

        @JsonProperty("params")
        public Map<String, Object> params;

        public Command() {}

        public Command(String method, Map<String, Object> params) {
            this.id = nextId();
            this.method = method;
            this.params = params;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Response {
        @JsonProperty("id")
        public Long id;

        @JsonProperty("result")
        public JsonNode result;

        @JsonProperty("error")
        public ErrorData error;

        @JsonProperty("method")
        public String method;

        @JsonProperty("params")
        public JsonNode params;

        public boolean isError() {
            return error != null;
        }

        public boolean isEvent() {
            return id == null && method != null;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ErrorData {
        @JsonProperty("error")
        public String error;

        @JsonProperty("message")
        public String message;

        @JsonProperty("stacktrace")
        public String stacktrace;

        @Override
        public String toString() {
            return error + ": " + message;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SessionStatus {
        @JsonProperty("ready")
        public boolean ready;

        @JsonProperty("message")
        public String message;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BrowsingContextInfo {
        @JsonProperty("context")
        public String context;

        @JsonProperty("url")
        public String url;

        @JsonProperty("children")
        public BrowsingContextInfo[] children;

        @JsonProperty("parent")
        public String parent;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class NavigateResult {
        @JsonProperty("navigation")
        public String navigation;

        @JsonProperty("url")
        public String url;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ScreenshotResult {
        @JsonProperty("data")
        public String data;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RealmInfo {
        @JsonProperty("realm")
        public String realm;

        @JsonProperty("origin")
        public String origin;

        @JsonProperty("type")
        public String type;

        @JsonProperty("context")
        public String context;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EvaluateResult {
        @JsonProperty("type")
        public String type;

        @JsonProperty("realm")
        public String realm;

        @JsonProperty("result")
        public RemoteValue result;

        @JsonProperty("exceptionDetails")
        public ExceptionDetails exceptionDetails;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RemoteValue {
        @JsonProperty("type")
        public String type;

        @JsonProperty("value")
        public Object value;

        @JsonProperty("sharedId")
        public String sharedId;

        public String getStringValue() {
            if (value == null) return null;
            return value.toString();
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ExceptionDetails {
        @JsonProperty("text")
        public String text;

        @JsonProperty("exception")
        public RemoteValue exception;
    }
}

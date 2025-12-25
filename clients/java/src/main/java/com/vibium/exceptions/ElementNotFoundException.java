package com.vibium.exceptions;

/**
 * Exception thrown when an element is not found.
 * Mirrors clients/javascript/src/utils/errors.ts ElementNotFoundError
 */
public class ElementNotFoundException extends VibiumException {
    private final String selector;

    public ElementNotFoundException(String selector) {
        super("Element not found: " + selector);
        this.selector = selector;
    }

    public String getSelector() {
        return selector;
    }
}

package com.vibium.clicker.bidi;

public class ElementInfo {
    public String tagName;
    public String textContent;
    public double x;
    public double y;
    public double width;
    public double height;

    public double getCenterX() {
        return x + width / 2;
    }

    public double getCenterY() {
        return y + height / 2;
    }

    @Override
    public String toString() {
        return String.format("<%s> \"%s\" at (%.0f, %.0f) size %.0fx%.0f",
            tagName, textContent, x, y, width, height);
    }
}

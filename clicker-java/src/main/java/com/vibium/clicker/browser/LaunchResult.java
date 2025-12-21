package com.vibium.clicker.browser;

public record LaunchResult(
    String wsUrl,
    Process chromedriverProcess,
    int port
) {
}

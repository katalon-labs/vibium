package com.vibium;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the Vibium Java client.
 * These tests require the clicker binary to be available.
 */
class BrowserTest {

    /**
     * Test basic browser launch, navigation, and screenshot.
     * Mirrors the JavaScript client test.
     */
    @Test
    void testLaunchNavigateScreenshot() throws Exception {
        // Skip if clicker binary not available
        try {
            com.vibium.clicker.BinaryResolver.resolve();
        } catch (Exception e) {
            System.out.println("Skipping test: clicker binary not found");
            return;
        }

        try (Vibe vibe = Browser.launch()) {
            // Navigate to example.com
            vibe.go("https://example.com");

            // Take a screenshot
            byte[] screenshot = vibe.screenshot();

            // Verify screenshot is a valid PNG (starts with PNG magic bytes)
            assertNotNull(screenshot);
            assertTrue(screenshot.length > 100, "Screenshot should have content");
            assertEquals((byte) 0x89, screenshot[0], "PNG magic byte 1");
            assertEquals((byte) 0x50, screenshot[1], "PNG magic byte 2 (P)");
            assertEquals((byte) 0x4E, screenshot[2], "PNG magic byte 3 (N)");
            assertEquals((byte) 0x47, screenshot[3], "PNG magic byte 4 (G)");
        }
    }

    /**
     * Test with headed mode (visible browser window).
     */
    @Test
    void testHeadedMode() throws Exception {
        // Skip if clicker binary not available
        try {
            com.vibium.clicker.BinaryResolver.resolve();
        } catch (Exception e) {
            System.out.println("Skipping test: clicker binary not found");
            return;
        }

        LaunchOptions options = new LaunchOptions().headless(false);

        try (Vibe vibe = Browser.launch(options)) {
            vibe.go("https://example.com");
            byte[] screenshot = vibe.screenshot();
            assertNotNull(screenshot);
            assertTrue(screenshot.length > 100);
        }
    }
}

import com.vibium.Browser;
import com.vibium.Element;
import com.vibium.Vibe;

import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Example: Find and click a link on a page.
 *
 * Usage:
 *   cd clients/java
 *   mvn compile exec:java -Dexec.mainClass="ClickLink"
 */
public class ClickLink {
    public static void main(String[] args) throws IOException {
        System.out.println("Launching browser...");

        try (Vibe vibe = Browser.launch()) {
            System.out.println("Navigating to example.com...");
            vibe.go("https://example.com");

            System.out.println("Finding link...");
            Element link = vibe.find("a");
            System.out.println("Found: " + link.getInfo().tag() + " - " + link.getInfo().text());

            System.out.println("Clicking link...");
            link.click();

            // Wait a moment for navigation
            Thread.sleep(1000);

            System.out.println("Taking screenshot...");
            byte[] screenshot = vibe.screenshot();

            try (FileOutputStream fos = new FileOutputStream("after-click.png")) {
                fos.write(screenshot);
            }

            System.out.println("Screenshot saved to after-click.png (" + screenshot.length + " bytes)");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

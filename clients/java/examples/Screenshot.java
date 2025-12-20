import com.vibium.Browser;
import com.vibium.LaunchOptions;
import com.vibium.Vibe;

import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Example: Take a screenshot of a website.
 *
 * Usage:
 *   cd clients/java
 *   mvn compile exec:java -Dexec.mainClass="Screenshot"
 *
 * Or compile and run directly:
 *   javac -cp target/classes examples/Screenshot.java -d target/classes
 *   java -cp target/classes:target/dependency/* Screenshot
 */
public class Screenshot {
    public static void main(String[] args) throws IOException {
        System.out.println("Launching browser...");

        try (Vibe vibe = Browser.launch()) {
            System.out.println("Navigating to example.com...");
            vibe.go("https://example.com");

            System.out.println("Taking screenshot...");
            byte[] screenshot = vibe.screenshot();

            // Save to file
            try (FileOutputStream fos = new FileOutputStream("screenshot.png")) {
                fos.write(screenshot);
            }

            System.out.println("Screenshot saved to screenshot.png (" + screenshot.length + " bytes)");
        }
    }
}

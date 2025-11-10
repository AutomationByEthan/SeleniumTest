// File: RegressionTest.java
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.*;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.OutputType;
import org.junit.jupiter.api.Test;

import org.apache.commons.io.FileUtils;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RegressionTest {

    private static final String SCENARIO = "01_Regression";
    private static int screenshotCounter = 0;
    private static String resultsDir;
    private static File logFile;
    private static WebDriver driver;   // only one driver

    public static void main(String[] args) {
        // ---- 1. URL from Jenkins (fallback = dummy site) ----
        String baseUrl = System.getProperty("test.url",
                "https://AutomationByEthan.github.io/Website-Regression/");

        // ---- 2. Chrome (headless, CI‑friendly) ----
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-gpu");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--window-size=1920,1080");

        driver = new ChromeDriver(options);

        try {
            // ---- 3. CREATE RESULTS FOLDER & LOG FIRST ----
            setupFoldersAndLog();
            appendToLog("STARTED: " + baseUrl + "\n");

            driver.get(baseUrl);
            appendToLog("Opened: " + baseUrl + "\n");

            // ---- 4. OPTIONAL: fail fast if redirected ----
            String current = driver.getCurrentUrl();
            if (!current.contains("Website-Regression")) {
                throw new Exception("REDIRECTED! URL: " + current);
            }

            // ---- 5. Give page a moment (you can replace with WebDriverWait later) ----
            Thread.sleep(3000);

            // ---- 6. TITLE CHECK (exact dummy title) ----
            String title = driver.getTitle().trim();
            if (title.contains("Kyperian Automation | UI Automation That Never Breaks")) {
                System.out.println("Title check PASSED");
                appendToLog("\t- Validation 01: Webpage appears with correct title\n");
            } else {
                throw new Exception("Title mismatch: " + title);
            }

            // ---- 7. SCREENSHOT ----
            takeScreenshot("Homepage");
            appendToLog("\t- Screenshot: Homepage.jpg\n");

            // ---- 8. SUCCESS ----
            appendToLog("\n============================== [ END ] ==============================\n");
            System.out.println("\nFULL SCRIPT EXECUTED SUCCESSFULLY!");
            System.out.println("Results folder: " + resultsDir);

        } catch (Exception e) {
            System.err.println("TEST FAILED: " + e.getMessage());
            e.printStackTrace();
            try { takeScreenshot("FAILURE"); } catch (Exception ignored) {}
            appendToLog("ERROR: " + e.getMessage() + "\n");
            System.exit(1);                     // <-- fail Jenkins build
        } finally {
            if (driver != null) driver.quit();
        }
    }

    // -----------------------------------------------------------------
    // 1. Create ./results/<date>/<scenario>/Chrome/<time>  (Jenkins workspace)
    // -----------------------------------------------------------------
    private static void setupFoldersAndLog() throws IOException {
        String today = new SimpleDateFormat("MM-dd-yyyy").format(new Date());
        String time  = new SimpleDateFormat("hhmm_a").format(new Date()).replace(" ", "");
        resultsDir = "./results/" + today + "/" + SCENARIO + "/Chrome/" + time;
        new File(resultsDir).mkdirs();

        logFile = new File(resultsDir + "/Execution_Log.txt");
        try (FileWriter w = new FileWriter(logFile)) {
            w.write("============================== [ " + SCENARIO + " ] ==============================\n");
            w.write("Purpose: Basic Regression on Dummy Site\n");
            w.write("Start: " + new SimpleDateFormat("MM/dd/yyyy hh:mm a").format(new Date()) + "\n\n");
        }
    }

    // -----------------------------------------------------------------
    // 2. Screenshot → resultsDir/Pic<#>_<name>.jpg
    // -----------------------------------------------------------------
    private static void takeScreenshot(String name) throws IOException {
        screenshotCounter++;
        File src  = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        File dest = new File(resultsDir + "/Pic" + screenshotCounter + "_" + name + ".jpg");
        FileUtils.copyFile(src, dest);               // renameTo fails in CI
        System.out.println("Screenshot: " + dest.getName());
    }

    // -----------------------------------------------------------------
    // 3. Append line to Execution_Log.txt
    // -----------------------------------------------------------------
    private static void appendToLog(String text) {
        try (FileWriter w = new FileWriter(logFile, true)) {
            w.write(text);
        } catch (IOException e) {
            System.err.println("Log write failed: " + e.getMessage());
        }
    }
}
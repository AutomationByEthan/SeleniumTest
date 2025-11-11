package com.example;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RegressionTest {

    private static final String SCENARIO = "01_Regression";
    private static int screenshotCounter = 0;
    private static String resultsDir;
    private static File logFile;
    private WebDriver driver;

    @BeforeAll
    void setupFoldersAndLog() throws IOException {
        String today = new SimpleDateFormat("MM-dd-yyyy").format(new Date());
        String time  = new SimpleDateFormat("hhmm_a").format(new Date()).replace(" ", "");
        resultsDir = "results/" + today + "/" + SCENARIO + "/Chrome/" + time;
        new File(resultsDir).mkdirs();

        logFile = new File(resultsDir + "/Execution_Log.txt");
        try (FileWriter w = new FileWriter(logFile)) {
            w.write("============================== [ " + SCENARIO + " ] ==============================\n");
            w.write("Purpose: Basic Regression on Dummy Site\n");
            w.write("Start: " + new SimpleDateFormat("MM/dd/yyyy hh:mm a").format(new Date()) + "\n\n");
        }
    }

    @BeforeEach
    void setupDriver() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new", "--no-sandbox", "--disable-gpu", "--disable-dev-shm-usage", "--window-size=1920,1080");
        driver = new ChromeDriver(options);
    }

    @Test
    void testRegressionSite() {
        String baseUrl = System.getProperty("test.url", "https://AutomationByEthan.github.io/Website-Regression/");

        try {
            appendToLog("STARTED: " + baseUrl + "\n");
            driver.get(baseUrl);
            appendToLog("Opened: " + baseUrl + "\n");

            String current = driver.getCurrentUrl();
            if (!current.contains("Website-Regression")) {
                throw new Exception("REDIRECTED! URL: " + current);
            }

            Thread.sleep(3000);

            String title = driver.getTitle().trim();
            if (title.contains("Kyperian Automation | UI Automation That Never Breaks")) {
                System.out.println("Title check PASSED");
                appendToLog("\t- Validation 01: Webpage appears with correct title\n");
            } else {
                throw new Exception("Title mismatch: " + title);
            }

            takeScreenshot("Homepage");
            appendToLog("\t- Screenshot: Homepage.jpg\n");

            appendToLog("\n============================== [ END ] ==============================\n");
            System.out.println("\nFULL SCRIPT EXECUTED SUCCESSFULLY!");
            System.out.println("Results folder: " + resultsDir);

        } catch (Exception e) {
            System.err.println("TEST FAILED: " + e.getMessage());
            e.printStackTrace();
            try { takeScreenshot("FAILURE"); } catch (Exception ignored) {}
            appendToLog("ERROR: " + e.getMessage() + "\n");
            fail(e);
        }
    }

    @AfterEach
    void tearDown() {
        if (driver != null) driver.quit();
    }

    private void takeScreenshot(String name) throws IOException {
        screenshotCounter++;
        File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        File dest = new File(resultsDir + "/Pic" + screenshotCounter + "_" + name + ".jpg");
        FileUtils.copyFile(src, dest);
        System.out.println("Screenshot: " + dest.getName());
    }

    private void appendToLog(String text) {
        try (FileWriter w = new FileWriter(logFile, true)) {
            w.write(text);
        } catch (IOException e) {
            System.err.println("Log write failed: " + e.getMessage());
        }
    }
}

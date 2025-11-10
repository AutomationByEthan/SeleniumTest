package com.example;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.time.Duration;
import io.github.bonigarcia.wdm.WebDriverManager;


public class ScriptRunner {
    static String scenario = "01_Login and Out";
    static int screenshotCounter = 0;
    static String filePath;
    static File logFile;
    static WebDriver driver;

    // === YOUR CREDENTIALS (Replace with real ones or use env vars later) ===
    static String VALID_EMAIL = "your_real_email@example.com";  // REPLACE
    static String VALID_PASSWORD = "your_real_password";        // REPLACE

    public static void main(String[] args) {
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-gpu");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--window-size=1920,1080");

        driver = new ChromeDriver(options);

        try {
            // === SETUP ===
            driver.get("https://app.todoist.com");
            driver.manage().window().setSize(new org.openqa.selenium.Dimension(1920, 1080));
            Thread.sleep(3000);

            setupFoldersAndLog();
            takeScreenshot(driver, "Login Page");

            // === INVALID LOGIN (Step 3) ===
            driver.findElement(By.cssSelector("input[type='email']")).sendKeys("LetsCreateAnErrorMessage@gmail.com");
            driver.findElement(By.cssSelector("input[type='password']")).sendKeys("LetsCreateAnErrorMessage");
            driver.findElement(By.cssSelector("button[type='submit']")).click();

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            WebElement errorElement = wait.until(
                    ExpectedConditions.visibilityOfElementLocated(
                            By.xpath("//div[text()=\"Wrong email or password.\"]")
                    )
            );
            String errorText = errorElement.getText().trim();
            takeScreenshot(driver, "Invalid Login Error Message");

            if (errorText.equals("Wrong email or password.")) {
                System.out.println("VALIDATION 01 PASSED");
                appendToLog("\n\t\t- Validation 01: Invalid Login Error Message Returns as Expected \n");
            } else {
                throw new Exception("Invalid login message mismatch");
            }

            // === VALID LOGIN ===
            System.out.println("\n--- Attempting Valid Login ---");

            // Clear fields
            WebElement emailField = driver.findElement(By.cssSelector("input[type='email']"));
            emailField.click();
            emailField.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);

            WebElement passwordField = driver.findElement(By.cssSelector("input[type='password']"));
            passwordField.click();
            passwordField.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);

            // Enter valid credentials
            driver.findElement(By.cssSelector("input[type='email']")).sendKeys("elgin.ethan@gmail.com");
            driver.findElement(By.cssSelector("input[type='password']")).sendKeys("thisIsMyPassword");
            takeScreenshot(driver, "Corrected Login Info");
            driver.findElement(By.cssSelector("button[type='submit']")).click();

            // Wait for home page
            wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.xpath("//button[@aria-label='Settings']")
            ));
            Thread.sleep(3000);  // Let UI settle
            takeScreenshot(driver, "Home Page");

            // === OPEN ACCOUNT DROPDOWN ===
            driver.findElement(By.xpath("//button[@aria-label='Settings']")).click();
            Thread.sleep(1000);
            takeScreenshot(driver, "Account Dropdown");

            // Get username
            WebElement userElement = driver.findElement(By.xpath("(//div[@role=\"menuitem\"]/span/span)[1]"));
            String loginUserName = userElement.getText().trim();
            appendToLog("\n\tUser: " + loginUserName + "\n");

            // === LOGOUT ===
            driver.findElement(By.xpath("//span[text()=\"Log out\"]")).click();

            // Wait for login page to return
            wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.xpath("//h1[text()='Welcome back!']")
            ));
            Thread.sleep(2000);
            takeScreenshot(driver, "Logout Screen");

            // === FINAL VALIDATION — USING REAL TEXT ===
            String titleText = driver.findElement(By.xpath("//h1[text()='Welcome back!']")).getText();
            if (titleText.contains("Welcome back!")) {
                System.out.println("VALIDATION 02 PASSED: Successfully logged out");
                appendToLog("\n\t\t- Validation 02: The User Successfully Logged Out of the Application \n");
            } else {
                throw new Exception("Logout failed — expected 'Welcome back!' page");
            }

            // === FINAL LOG ===
            appendToLog("\n============================== [ End of Automation Execution ] ==============================\n\n");

            System.out.println("\nFULL SCRIPT EXECUTED SUCCESSFULLY!");
            System.out.println("Check folder: " + filePath);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Close browser
            if (driver != null) {
                driver.quit();
            }
        }
    }

    // === HELPER METHODS ===
    static void setupFoldersAndLog() throws Exception {
        String today = new SimpleDateFormat("MM-dd-yyyy").format(new Date());
        String time = new SimpleDateFormat("hhmm a").format(new Date());
        filePath = "C:/Katalon/DemoScripts/" + today + "/" + scenario + "/Chrome/" + time;
        new File(filePath).mkdirs();

        logFile = new File(filePath + "/Text File.txt");
        try (FileWriter writer = new FileWriter(logFile)) {
            writer.write("\n============================== [ Automation Execution of " + scenario + " ] ==============================\n");
            writer.write("\n\tScript Purpose: Login and logout. Validations will be made to ensure proper screens display.\n");
        }
    }

    static void takeScreenshot(WebDriver driver, String name) throws Exception {
        screenshotCounter++;
        File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        File dest = new File(filePath + "/Pic " + screenshotCounter + " - " + name + ".jpg");
        src.renameTo(dest);
    }

    static void appendToLog(String text) {
        try (FileWriter writer = new FileWriter(logFile, true)) {
            writer.write(text);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
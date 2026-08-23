package com.adesso.qa.driver;

import java.time.Duration;
import java.util.logging.Level;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.bonigarcia.wdm.WebDriverManager;

public class DriverManager {

    private static final Logger log = LoggerFactory.getLogger(DriverManager.class);
    private static final ThreadLocal<WebDriver> driverThreadLocal = new ThreadLocal<>();

    // Static block to silence CDP warnings globally before any driver initializes
    static {
        java.util.logging.Logger.getLogger("org.openqa.selenium").setLevel(Level.SEVERE);
    }

    private DriverManager() {
        // Prevent direct instantiation
    }

    public static WebDriver getDriver() {
        if (driverThreadLocal.get() == null) {
            String browserProperty = System.getProperty("browser", "CHROME").toUpperCase();
            DriverType driverType;

            try {
                driverType = DriverType.valueOf(browserProperty);
            } catch (IllegalArgumentException e) {
                log.warn("Unknown browser '{}' specified. Defaulting to CHROME.", browserProperty);
                driverType = DriverType.CHROME;
            }

            boolean isHeadless = Boolean.parseBoolean(System.getProperty("headless", "false"));
            log.info("Creating [{}] driver instance (Headless: {})", driverType, isHeadless);

            WebDriver driver = createDriver(driverType, isHeadless);

            // Set explicit Dimension directly (works reliably in headless & headful)
            driver.manage().window().setSize(new org.openqa.selenium.Dimension(1920, 1080));
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

            driverThreadLocal.set(driver);
        }
        return driverThreadLocal.get();
    }

    private static WebDriver createDriver(DriverType type, boolean isHeadless) {
        switch (type) {
            case FIREFOX:
                WebDriverManager.firefoxdriver().setup();
                FirefoxOptions firefoxOptions = new FirefoxOptions();

                if (isHeadless) {
                    firefoxOptions.addArguments("--headless");
                    firefoxOptions.addArguments("--width=1920");
                    firefoxOptions.addArguments("--height=1080");
                }
                firefoxOptions.addArguments("-private");
                return new FirefoxDriver(firefoxOptions);

            case CHROME:
            default:
                WebDriverManager.chromedriver().setup();
                ChromeOptions chromeOptions = new ChromeOptions();
                chromeOptions.addArguments("--remote-allow-origins=*");
                chromeOptions.addArguments("--lang=en-US");

                // Always pass window-size argument to ChromeOptions
                chromeOptions.addArguments("--window-size=1920,1080");

                if (isHeadless) {
                    chromeOptions.addArguments("--headless=new");
                    chromeOptions.addArguments("--no-sandbox");
                    chromeOptions.addArguments("--disable-dev-shm-usage");
                }
                return new ChromeDriver(chromeOptions);
        }
    }
    public static void quitDriver() {
        if (driverThreadLocal.get() != null) {
            driverThreadLocal.get().quit();
            driverThreadLocal.remove();
        }
    }
}
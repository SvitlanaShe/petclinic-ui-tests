package com.adesso.qa.tests;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adesso.qa.driver.DriverManager;
import com.adesso.qa.listeners.RetryExtension;
import com.adesso.qa.listeners.TestListener;


@ExtendWith({TestListener.class, RetryExtension.class})
public abstract class BaseTest {

    protected static final String BASE_URL = System.getProperty("baseUrl", "http://localhost:8080");
    protected final Logger log = LoggerFactory.getLogger(getClass());
    protected WebDriver driver;
    protected WebDriverWait wait;

    // Bootstrap navbar toggler selector
    private final By navbarToggler = By.cssSelector("button.navbar-toggler");

    @BeforeEach
    public void setUp() {
        log.info("Starting test execution on thread [{}]", Thread.currentThread().getName());
        driver = DriverManager.getDriver();
    }

    /**
     * Safely clicks navigation links even if collapsed into a mobile menu.
     */
    protected void clickNavMenu() {
        // 1. If mobile hamburger button is displayed, click to expand the navbar menu
        try {
            List<WebElement> togglers = driver.findElements(navbarToggler);

            if (!togglers.isEmpty() && togglers.get(0).isDisplayed()) {
                togglers.get(0).click();
            }

            // 2. Wait until link is visible and interactable, then click
            wait.until(ExpectedConditions.elementToBeClickable(navbarToggler)).click();
        } catch (Exception e) {
            System.out.println("No toggle is visible on the page!");
        }
    }

}
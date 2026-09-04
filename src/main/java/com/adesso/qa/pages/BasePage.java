package com.adesso.qa.pages;

import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adesso.qa.driver.DriverManager;

public abstract class BasePage {

    protected WebDriver driver;
    protected WebDriverWait wait;
    protected final Logger log = LoggerFactory.getLogger(getClass());

    // Single source of truth for success alert locator
    private final By successAlert = By.cssSelector("#success-message span");

    // Top Navigation Bar Locators
    @FindBy(css = "a[title='home page']")
    private WebElement homeNavButton;

    @FindBy(css = "a[title='find owners']")
    private WebElement findOwnersNavButton;

    @FindBy(css = "a[title='veterinarians']")
    private WebElement veterinariansNavButton;

    @FindBy(css = "a[title='trigger a RuntimeException to see how it is handled']")
    private WebElement errorNavButton;

    public BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        PageFactory.initElements(driver, this);
    }

    // Default constructor option using Singleton
    public BasePage() {
        this(DriverManager.getDriver());
    }

    // --- Navigation Methods ---

    public HomePage clickHomeMenu() {
        log.info("Clicking 'HOME' in header navigation menu.");
        homeNavButton.click();
        return new HomePage(driver);
    }

    public FindOwnersPage clickFindOwnersMenu() {
        log.info("Clicking 'FIND OWNERS' in header navigation menu.");
        findOwnersNavButton.click();
        return new FindOwnersPage(driver);
    }

    public VetsPage clickVeterinariansMenu() {
        log.info("Clicking 'VETERINARIANS' in header navigation menu.");
        veterinariansNavButton.click();
        return new VetsPage(driver);
    }

    public ErrorPage clickErrorMenu() {
        log.info("Clicking 'ERROR' in header navigation menu.");
        errorNavButton.click();
        return new ErrorPage(driver);
    }

    // --- URL Helper Methods ---

    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    public boolean isAt(String expectedPath) {
        String currentUrl = getCurrentUrl();
        log.info("Checking if current URL [{}] contains path [{}]", currentUrl, expectedPath);
        return currentUrl.contains(expectedPath);
    }

    // --- JavaScript Interaction Helpers ---

    protected void scrollIntoView(WebElement element) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", element);
    }

    protected void clickRobustly(WebElement element) {
        try {
            element.click();
        } catch (ElementClickInterceptedException e) {
            log.warn("Native click was intercepted; falling back to JavaScript click", e);
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
        }
    }

    protected void clickWithScrollAndFallback(WebElement element) {
        scrollIntoView(element);
        clickRobustly(element);
    }

    protected void setValueViaJavaScript(WebElement field, String value) {
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].value = arguments[1];"
                        + "arguments[0].dispatchEvent(new Event('change', { bubbles: true }));",
                field, value);
    }

    // --- Banner Assertions ---

    /**
     * Captures the text inside the success banner safely, even if it autodisappears.
     */
    public String getSuccessMessageText() {
        WebElement messageSpan = wait.until(
                ExpectedConditions.presenceOfElementLocated(successAlert)
        );
        return messageSpan.getAttribute("textContent").trim();
    }
}
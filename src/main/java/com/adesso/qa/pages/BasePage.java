package com.adesso.qa.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adesso.qa.driver.DriverManager;

public abstract class BasePage {

    protected WebDriver driver;
    protected final Logger log = LoggerFactory.getLogger(getClass());

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
}
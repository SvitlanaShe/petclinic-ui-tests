package com.adesso.qa.pages;

import java.time.Duration;
import java.util.Objects;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.adesso.qa.driver.DriverManager;

/**
 * Page Object for the "Add Visit" form.
 *
 * <p>Encapsulates every locator and interaction needed to record a new
 * visit and exposes a single, intention-revealing entry point,
 * {@link #addVisit(String)}, to test classes. Callers should not need to
 * know the underlying field ids, locators, or wait strategy.</p>
 */
public class AddVisitPage extends BasePage {

    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(10);

    @FindBy(id = "description")
    private WebElement descriptionInput;

    @FindBy(css = "button[type='submit']")
    private WebElement submitButton;

    private final WebDriverWait wait;

    public AddVisitPage(WebDriver driver) {
        super(driver);
        this.wait = new WebDriverWait(driver, DEFAULT_TIMEOUT);
    }

    // Default constructor option using Singleton, consistent with BasePage.
    public AddVisitPage() {
        this(DriverManager.getDriver());
    }

    /**
     * Fills out and submits the "Add Visit" form, then waits for navigation
     * back to the owner details page.
     *
     * @param description the visit description; must not be {@code null}
     * @throws IllegalArgumentException if {@code description} is {@code null}
     */
    public void addVisit(String description) {
        Objects.requireNonNull(description, "description must not be null");

        log.info("Entering visit description: '{}'", description);

        WebElement field = wait.until(ExpectedConditions.visibilityOf(descriptionInput));
        field.clear();
        field.sendKeys(description);

        clickAddVisit();

        wait.until(ExpectedConditions.urlContains("/owners/"));
        log.info("Visit added successfully, navigated to: {}", driver.getCurrentUrl());
    }

    /**
     * Clicks the "Add Visit" submit button, scrolling it into view first
     * and falling back to a JavaScript click if the native click is
     * intercepted (e.g. by an overlay or sticky header).
     *
     * <p>Exposed separately from {@link #addVisit(String)} so tests can
     * submit the form without a description, e.g. to verify validation
     * errors.</p>
     */
    public void clickAddVisit() {
        WebElement button = wait.until(ExpectedConditions.elementToBeClickable(submitButton));
        clickWithScrollAndFallback(button);
    }
}
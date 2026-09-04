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
 * Page Object for the "Find Owners" search form (path {@value #PATH}).
 *
 * <p>Encapsulates every locator and interaction needed to search for an
 * owner by last name and to navigate onward to the "Add Owner" form.</p>
 */
public class FindOwnersPage extends BasePage {

    public static final String PATH = "/owners/find";

    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(10);

    @FindBy(id = "lastName")
    private WebElement lastNameInput;

    @FindBy(css = "button[type='submit']")
    private WebElement findOwnerButton;

    @FindBy(linkText = "Add Owner")
    private WebElement addOwnerButton;

    @FindBy(css = ".help-block")
    private WebElement helpBlock;

    private final WebDriverWait wait;

    public FindOwnersPage(WebDriver driver) {
        super(driver);
        this.wait = new WebDriverWait(driver, DEFAULT_TIMEOUT);
    }

    // Default constructor option using Singleton, consistent with BasePage.
    public FindOwnersPage() {
        this(DriverManager.getDriver());
    }

    /**
     * Checks whether the browser is currently on this page.
     *
     * @return {@code true} if the current URL contains {@link #PATH}
     */
    public boolean isAtPage() {
        return isAt(PATH);
    }

    /**
     * Searches for an owner by last name.
     *
     * <p>Depending on the number of matches, the application may redirect
     * straight to the owner's details, show a results list, or redisplay
     * this form with a validation error — so this method intentionally
     * returns {@code void} rather than a specific next page. Use
     * {@link #getHelpBlockText()} or the appropriate page object to assert
     * on the outcome.</p>
     *
     * @param lastName the last name to search for; must not be {@code null}
     * @throws IllegalArgumentException if {@code lastName} is {@code null}
     */
    public void searchOwner(String lastName) {
        Objects.requireNonNull(lastName, "lastName must not be null");

        log.info("Searching for owner with last name '{}'", lastName);

        WebElement field = wait.until(ExpectedConditions.visibilityOf(lastNameInput));
        field.clear();
        field.sendKeys(lastName);

        wait.until(ExpectedConditions.elementToBeClickable(findOwnerButton)).click();
    }

    /**
     * Navigates to the "Add Owner" form.
     *
     * @return the resulting {@link AddOwnerPage}
     */
    public AddOwnerPage clickAddOwner() {
        log.info("Clicking 'Add Owner' link.");
        wait.until(ExpectedConditions.elementToBeClickable(addOwnerButton)).click();
        return new AddOwnerPage(driver);
    }

    /**
     * Returns the text of the field-level validation error (e.g. "not
     * found"), waiting for it to become visible.
     *
     * @return the help block text
     * @throws org.openqa.selenium.TimeoutException if no error becomes
     *         visible within the configured timeout
     */
    public String getHelpBlockText() {
        String message = wait.until(ExpectedConditions.visibilityOf(helpBlock)).getText();
        log.debug("Captured help block text: '{}'", message);
        return message;
    }
}
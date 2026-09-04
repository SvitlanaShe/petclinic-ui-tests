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
 * Page Object for the "Add Owner" form.
 *
 * <p>Encapsulates every locator and interaction needed to create a new
 * owner record and exposes a single, intention-revealing entry point,
 * {@link #createOwner(OwnerDetails)}, to test classes. Callers should not
 * need to know the underlying field ids, locators, or wait strategy.</p>
 */
public class AddOwnerPage extends BasePage {

    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(10);

    @FindBy(id = "firstName")
    private WebElement firstNameInput;

    @FindBy(id = "lastName")
    private WebElement lastNameInput;

    @FindBy(id = "address")
    private WebElement addressInput;

    @FindBy(id = "city")
    private WebElement cityInput;

    @FindBy(id = "telephone")
    private WebElement telephoneInput;

    @FindBy(css = "button[type='submit']")
    private WebElement submitButton;

    private final WebDriverWait wait;

    public AddOwnerPage(WebDriver driver) {
        super(driver);
        this.wait = new WebDriverWait(driver, DEFAULT_TIMEOUT);
    }

    // Default constructor option using Singleton, consistent with BasePage.
    public AddOwnerPage() {
        this(DriverManager.getDriver());
    }

    /**
     * Fills out and submits the "Add Owner" form, then waits for navigation
     * to the resulting owner details page.
     *
     * @param owner the data to submit; must not be {@code null}
     * @throws IllegalArgumentException if {@code owner} is {@code null}
     */
    public void createOwner(OwnerDetails owner) {
        Objects.requireNonNull(owner, "owner must not be null");

        log.debug("Creating owner '{} {}'", owner.firstName(), owner.lastName());

        type(firstNameInput, owner.firstName());
        type(lastNameInput, owner.lastName());
        type(addressInput, owner.address());
        type(cityInput, owner.city());
        type(telephoneInput, owner.telephone());

        submit();

        log.info("Owner '{} {}' created successfully", owner.firstName(), owner.lastName());
    }

    /**
     * Convenience overload for existing call sites.
     *
     * @deprecated prefer {@link #createOwner(OwnerDetails)} for clearer,
     *             self-documenting call sites and easier test-data reuse.
     */
    @Deprecated
    public void createOwner(String firstName, String lastName, String address, String city, String telephone) {
        createOwner(new OwnerDetails(firstName, lastName, address, city, telephone));
    }

    private void type(WebElement field, String value) {
        WebElement visible = wait.until(ExpectedConditions.visibilityOf(field));
        visible.clear();
        visible.sendKeys(value);
    }

    private void submit() {
        WebElement button = wait.until(ExpectedConditions.elementToBeClickable(submitButton));
        clickRobustly(button);

        // Waiting on the URL is a pragmatic signal that submission succeeded;
        // swap for a visibility wait on an element unique to the owner
        // details page if the URL pattern is not stable enough in practice.
        wait.until(ExpectedConditions.urlContains("/owners/"));
    }

    /**
     * Immutable value object carrying the fields required to create an
     * owner. Using a dedicated type instead of five positional
     * {@code String} parameters removes ambiguity at call sites and makes
     * test data easy to build, reuse, and vary between test cases.
     */
    public record OwnerDetails(String firstName, String lastName, String address, String city, String telephone) {

        public OwnerDetails {
            Objects.requireNonNull(firstName, "firstName must not be null");
            Objects.requireNonNull(lastName, "lastName must not be null");
            Objects.requireNonNull(address, "address must not be null");
            Objects.requireNonNull(city, "city must not be null");
            Objects.requireNonNull(telephone, "telephone must not be null");
        }
    }
}
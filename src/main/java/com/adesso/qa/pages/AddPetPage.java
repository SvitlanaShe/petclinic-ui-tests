package com.adesso.qa.pages;

import java.time.Duration;
import java.util.Objects;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.adesso.qa.driver.DriverManager;

/**
 * Page Object for the "Add Pet" form.
 *
 * <p>Encapsulates every locator and interaction needed to create a new
 * pet record and exposes a single, intention-revealing entry point,
 * {@link #addPet(PetDetails)}, to test classes. Callers should not need to
 * know the underlying field ids, locators, or wait strategy.</p>
 */
public class AddPetPage extends BasePage {

    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(10);
    private static final By ERROR_LOCATOR = By.cssSelector(".help-block, .has-error, .alert");

    @FindBy(id = "name")
    private WebElement petNameInput;

    @FindBy(id = "birthDate")
    private WebElement birthDateInput;

    @FindBy(id = "type")
    private WebElement petTypeSelect;

    @FindBy(css = "button[type='submit']")
    private WebElement submitButton;

    private final WebDriverWait wait;

    public AddPetPage(WebDriver driver) {
        super(driver);
        this.wait = new WebDriverWait(driver, DEFAULT_TIMEOUT);
    }

    // Default constructor option using Singleton, consistent with BasePage.
    public AddPetPage() {
        this(DriverManager.getDriver());
    }

    /**
     * Fills out and submits the "Add Pet" form.
     *
     * @param pet the data to submit; must not be {@code null}
     * @throws IllegalArgumentException if {@code pet} is {@code null}
     */
    public void addPet(PetDetails pet) {
        Objects.requireNonNull(pet, "pet must not be null");

        log.info("Entering pet details: name={}, birthDate={}, type={}",
                pet.name(), pet.birthDate(), pet.type());

        type(petNameInput, pet.name());
        setDate(birthDateInput, pet.birthDate());

        wait.until(ExpectedConditions.visibilityOf(petTypeSelect));
        new Select(petTypeSelect).selectByVisibleText(pet.type());

        submit();
    }

    /**
     * Convenience overload for existing call sites.
     *
     * @deprecated prefer {@link #addPet(PetDetails)} for clearer,
     *             self-documenting call sites and easier test-data reuse.
     */
    @Deprecated
    public void addPet(String name, String birthDate, String type) {
        addPet(new PetDetails(name, birthDate, type));
    }

    /**
     * Returns the text of the first visible validation error on the page
     * (field-level {@code .help-block}, a {@code .has-error} block, or a
     * general {@code .alert}), waiting for it to appear.
     *
     * @return the error message text
     * @throws org.openqa.selenium.TimeoutException if no error becomes
     *         visible within the configured timeout
     */
    public String getErrorMessage() {
        WebElement errorElement = wait.until(ExpectedConditions.visibilityOfElementLocated(ERROR_LOCATOR));
        String message = errorElement.getText();
        log.debug("Captured validation error message: '{}'", message);
        return message;
    }

    private void type(WebElement field, String value) {
        WebElement visible = wait.until(ExpectedConditions.visibilityOf(field));
        visible.clear();
        visible.sendKeys(value);
    }

    private void setDate(WebElement field, String isoDate) {
        wait.until(ExpectedConditions.visibilityOf(field));
        // HTML5 date inputs often ignore sendKeys() due to locale-specific
        // rendering, so the value is set directly via JavaScript.
        setValueViaJavaScript(field, isoDate);
    }

    private void submit() {
        WebElement button = wait.until(ExpectedConditions.elementToBeClickable(submitButton));
        clickRobustly(button);
    }

    /**
     * Immutable value object carrying the fields required to create a pet.
     *
     * @param birthDate ISO-8601 date string ({@code yyyy-MM-dd}), matching
     *                  the format expected by the underlying HTML5 date input
     */
    public record PetDetails(String name, String birthDate, String type) {

        public PetDetails {
            Objects.requireNonNull(name, "name must not be null");
            Objects.requireNonNull(birthDate, "birthDate must not be null");
            Objects.requireNonNull(type, "type must not be null");
        }
    }
}
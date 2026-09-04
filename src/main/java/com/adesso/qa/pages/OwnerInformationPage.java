package com.adesso.qa.pages;

import java.time.Duration;
import java.util.Objects;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.adesso.qa.driver.DriverManager;
import com.adesso.qa.util.XPathUtils;

/**
 * Page Object for the owner details page ("Owner Information"), showing the
 * owner's data together with their pets and visits.
 */
public class OwnerInformationPage extends BasePage {

    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration VISIT_LINK_TIMEOUT = Duration.ofSeconds(20);
    private static final By OWNER_INFO_HEADER = By.xpath("//h2[contains(text(),'Owner Information')]");

    @FindBy(xpath = "//tr[th[contains(., 'Name')]]/td")
    private WebElement ownerName;

    @FindBy(xpath = "//a[contains(@href, 'pets/new') or normalize-space()='Add New Pet']")
    private WebElement addNewPetButton;

    private final WebDriverWait wait;
    private final WebDriverWait longWait;

    public OwnerInformationPage(WebDriver driver) {
        super(driver);
        this.wait = new WebDriverWait(driver, DEFAULT_TIMEOUT);
        this.longWait = new WebDriverWait(driver, VISIT_LINK_TIMEOUT);
    }

    // Default constructor option using Singleton, consistent with BasePage.
    public OwnerInformationPage() {
        this(DriverManager.getDriver());
    }

    /**
     * Clicks "Add New Pet" and navigates to the "Add Pet" form.
     *
     * @return the resulting {@link AddPetPage}
     */
    public AddPetPage clickAddNewPet() {
        log.info("Clicking 'Add New Pet'.");
        WebElement button = wait.until(ExpectedConditions.elementToBeClickable(addNewPetButton));
        clickWithScrollAndFallback(button); // inherited from BasePage
        return new AddPetPage(driver);
    }

    /**
     * Clicks the "Add Visit" link for the given pet and navigates to the
     * "Add Visit" form.
     *
     * @param petName the exact pet name as displayed on the page; must not
     *                be {@code null}
     * @return the resulting {@link AddVisitPage}
     * @throws IllegalArgumentException if {@code petName} is {@code null}
     */
    public AddVisitPage clickAddVisitForPet(String petName) {
        Objects.requireNonNull(petName, "petName must not be null");

        log.info("Clicking 'Add Visit' for pet '{}'.", petName);

        String literal = XPathUtils.literal(petName);
        By visitLinkLocator = By.xpath(
                "//dd[normalize-space()=" + literal + "]/ancestor::tr//a[contains(@href, '/visits/new')]"
                        + " | //td[contains(normalize-space(), " + literal + ")]/ancestor::tr//a[contains(@href, '/visits/new')]"
        );

        WebElement visitButton = longWait.until(ExpectedConditions.presenceOfElementLocated(visitLinkLocator));
        clickWithScrollAndFallback(visitButton);
        return new AddVisitPage(driver);
    }

    /**
     * Checks whether a pet with the given name is listed on the page.
     *
     * @param petName the pet name to look for; must not be {@code null}
     * @return {@code true} if a matching row is present
     */
    public boolean hasPet(String petName) {
        Objects.requireNonNull(petName, "petName must not be null");

        By petLocator = By.xpath(
                "//table[contains(@class, 'table')]//td[contains(., " + XPathUtils.literal(petName) + ")]");
        return !driver.findElements(petLocator).isEmpty();
    }

    /**
     * Checks whether a visit with the given description is listed on the
     * page, waiting for it to appear.
     *
     * @param description the visit description to look for; must not be
     *                     {@code null}
     * @return {@code true} if a matching, visible element is found within
     *         the timeout; {@code false} otherwise
     */
    public boolean hasVisitDescription(String description) {
        Objects.requireNonNull(description, "description must not be null");

        String literal = XPathUtils.literal(description);
        By visitLocator = By.xpath(
                "//td[contains(normalize-space(), " + literal + ")]"
                        + " | //dd[contains(normalize-space(), " + literal + ")]");

        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(OWNER_INFO_HEADER));
            WebElement visitElement = wait.until(ExpectedConditions.presenceOfElementLocated(visitLocator));
            scrollIntoView(visitElement);
            return visitElement.isDisplayed();
        } catch (TimeoutException e) {
            log.debug("Visit description '{}' did not appear within the timeout.", description);
            return false;
        }
    }

    /**
     * Returns the owner's full name as displayed on the page.
     *
     * @return the trimmed owner name
     */
    public String getOwnerName() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(OWNER_INFO_HEADER));
        return wait.until(ExpectedConditions.visibilityOf(ownerName)).getText().trim();
    }
}
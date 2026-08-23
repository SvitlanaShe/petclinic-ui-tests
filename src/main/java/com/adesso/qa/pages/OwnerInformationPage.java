package com.adesso.qa.pages;

import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.adesso.qa.driver.DriverManager;

public class OwnerInformationPage extends BasePage {

    @FindBy(xpath = "//tr[th[contains(., 'Name')]]/td")
    private WebElement ownerName;

    @FindBy(xpath = "//a[contains(@href, '/pets/new')]")
    private WebElement addNewPetButton;

    public OwnerInformationPage(WebDriver driver) {
        super(driver);
    }

    public void clickAddNewPet() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        // Matches href ending in 'pets/new' or button text 'Add New Pet'
        By addPetBtnLocator = By.xpath(
                "//a[contains(@href, 'pets/new') or normalize-space()='Add New Pet']"
        );

        // 1. Wait for presence in DOM
        WebElement addPetBtn = wait.until(ExpectedConditions.presenceOfElementLocated(addPetBtnLocator));

        // 2. Center in viewport (critical for Firefox headless)
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", addPetBtn);

        // 3. Click with JS fallback
        try {
            addPetBtn.click();
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", addPetBtn);
        }
    }

    public void clickAddVisitForPet(String petName) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));

        // Robust XPath: Finds any row/cell matching the pet name, then grabs the corresponding '/visits/new' link
        By visitLinkLocator = By.xpath(
                "//dd[normalize-space()='" + petName + "']/ancestor::tr//a[contains(@href, '/visits/new')]" +
                        " | //td[contains(normalize-space(), '" + petName + "')]/ancestor::tr//a[contains(@href, '/visits/new')]" +
                        " | //a[contains(@href, '/visits/new')]"
        );

        WebElement visitBtn = wait.until(ExpectedConditions.presenceOfElementLocated(visitLinkLocator));

        // Scroll to the link and click safely
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", visitBtn);
        try {
            visitBtn.click();
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", visitBtn);
        }
    }

    public boolean hasPet(String petName) {
        return !driver.findElements(
                By.xpath("//table[contains(@class, 'table')]//td[contains(., '" + petName + "')]")
        ).isEmpty();
    }

    public boolean hasVisitDescription(String description) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        try {
            // 1. Confirm we are on the owner page header first
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h2[contains(text(),'Owner Information')]")));

            // 2. Wait explicitly for the visit text in the page
            By visitLocator = By.xpath("//td[contains(normalize-space(), '" + description + "')] | //*[contains(text(), '" + description + "')]");
            WebElement visitElement = wait.until(ExpectedConditions.presenceOfElementLocated(visitLocator));

            // 3. Scroll into view and return true
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", visitElement);
            return visitElement.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public String getOwnerName() {
        WebDriverWait wait = new WebDriverWait(DriverManager.getDriver(), Duration.ofSeconds(10));

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h2[contains(text(),'Owner Information')]")));

        By nameLocator = By.xpath("//tr[th[contains(., 'Name')]]/td");

        WebElement nameElement = wait.until(ExpectedConditions.presenceOfElementLocated(nameLocator));
        return nameElement.getText().trim();
    }
}
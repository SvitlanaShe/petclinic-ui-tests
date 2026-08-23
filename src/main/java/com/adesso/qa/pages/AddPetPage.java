package com.adesso.qa.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;

public class AddPetPage extends BasePage {

    @FindBy(id = "name")
    private WebElement petNameInput;

    @FindBy(id = "birthDate")
    private WebElement birthDateInput;

    @FindBy(id = "type")
    private WebElement petTypeSelect;

    @FindBy(css = "button[type='submit']")
    private WebElement submitButton;

    @FindBy(css = ".help-block")
    private WebElement helpBlock;

    public AddPetPage(WebDriver driver) {
        super(driver);
    }

    public void addPet(String name, String birthDate, String type) {
        log.info("Entering pet details: Name={}, BirthDate={}, Type={}", name, birthDate, type);
        petNameInput.clear();
        petNameInput.sendKeys(name);

        // Set the HTML5 date input value directly via JavaScript
        JavascriptExecutor executor = (JavascriptExecutor) driver;
        executor.executeScript("arguments[0].value = arguments[1];", birthDateInput, birthDate);

        new Select(petTypeSelect).selectByVisibleText(type);
        submitButton.click();
    }

    public String getErrorMessage() {
        // Look for any standard Spring form field error block or general alert
        WebElement errorElement = driver.findElement(
                By.xpath("//span[contains(@class, 'help-block')] | //div[contains(@class, 'has-error')] | //*[contains(@class, 'alert')]")
        );
        return errorElement.getText();
    }
}
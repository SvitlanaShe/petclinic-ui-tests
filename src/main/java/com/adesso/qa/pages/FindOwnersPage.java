package com.adesso.qa.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class FindOwnersPage extends BasePage {
    // Define the page route as a constant inside its own page class
    public static final String PATH = "/owners/find";

    @FindBy(id = "lastName")
    private WebElement lastNameInput;

    @FindBy(css = "button[type='submit']")
    private WebElement findOwnerButton;

    @FindBy(linkText = "Add Owner")
    private WebElement addOwnerButton;

    @FindBy(css = ".help-block")
    private WebElement helpBlock;

    public FindOwnersPage(WebDriver driver) {
        super(driver);
    }

    public boolean isAtPage() {
        return isAt(PATH);
    }

    public void searchOwner(String lastName) {
        lastNameInput.clear();
        lastNameInput.sendKeys(lastName);
        findOwnerButton.click();
    }

    public void clickAddOwner() {
        addOwnerButton.click();
    }

    public String getHelpBlockText() {
        return helpBlock.getText();
    }
}
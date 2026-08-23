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

public class AddVisitPage extends BasePage {

    @FindBy(id = "description")
    private WebElement descriptionInput;

    @FindBy(css = "button[type='submit']")
    private WebElement submitButton;

    public AddVisitPage(WebDriver driver) {
        super(driver);
    }

    public void addVisit(String description) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        // 1. Fill description field
        WebElement descField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("description")));
        descField.clear();
        descField.sendKeys(description);

        clickAddVisit();
        // 2. Wait for redirect back to Owner Details page
        wait.until(ExpectedConditions.urlContains("/owners/"));
    }


    public void clickAddVisit() {
        WebDriverWait wait = new WebDriverWait(DriverManager.getDriver(), Duration.ofSeconds(10));

        // 1. Locate the "Add Visit" submit button
        WebElement submitBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("button.btn.btn-primary[type='submit'], button[type='submit']")
        ));

        // 2. Scroll into view and click natively (or fire event)
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", submitBtn);

        try {
            submitBtn.click();
        } catch (Exception e) {
            // Dispatches full click event tree if standard click is intercepted
            ((JavascriptExecutor) driver).executeScript(
                    "var evt = document.createEvent('MouseEvents');" +
                            "evt.initEvent('click', true, true);" +
                            "arguments[0].dispatchEvent(evt);", submitBtn
            );
        }

    }
}
package com.adesso.qa.pages;

import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class AddOwnerPage extends BasePage {

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

    public AddOwnerPage(WebDriver driver) {
        super(driver);
    }

    public void createOwner(String firstName, String lastName, String address, String city, String telephone) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("firstName"))).sendKeys(firstName);
        driver.findElement(By.id("lastName")).sendKeys(lastName);
        driver.findElement(By.id("address")).sendKeys(address);
        driver.findElement(By.id("city")).sendKeys(city);
        driver.findElement(By.id("telephone")).sendKeys(telephone);

        // Click submit button
        WebElement submitBtn = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//button[@type='submit' or contains(text(), 'Add Owner')]")
        ));

        try {
            submitBtn.click();
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", submitBtn);
        }

        // CRITICAL: Wait for browser navigation to complete before returning
        wait.until(ExpectedConditions.urlContains("/owners/"));
    }
}
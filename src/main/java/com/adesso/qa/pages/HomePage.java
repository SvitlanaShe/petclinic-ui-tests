package com.adesso.qa.pages;

import org.openqa.selenium.WebDriver;

import com.adesso.qa.driver.DriverManager;

/**
 * Page Object for the application's landing page (path {@value #PATH}).
 */
public class HomePage extends BasePage {

    public static final String PATH = "/";

    public HomePage(WebDriver driver) {
        super(driver);
    }

    // Default constructor option using Singleton, consistent with BasePage.
    public HomePage() {
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
}
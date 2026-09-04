package com.adesso.qa.pages;

import org.openqa.selenium.WebDriver;

import com.adesso.qa.driver.DriverManager;

/**
 * Page Object for the veterinarians listing page (path {@value #PATH}).
 */
public class VetsPage extends BasePage {

    public static final String PATH = "/vets.html";

    public VetsPage(WebDriver driver) {
        super(driver);
    }

    // Default constructor option using Singleton, consistent with BasePage.
    public VetsPage() {
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
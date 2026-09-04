package com.adesso.qa.pages;

import org.openqa.selenium.WebDriver;

import com.adesso.qa.driver.DriverManager;

/**
 * Page Object for the generic error page (path {@value #PATH}) shown by the
 * application's global exception handler after an unhandled
 * {@link RuntimeException}.
 */
public class ErrorPage extends BasePage {

    public static final String PATH = "/oups";

    public ErrorPage(WebDriver driver) {
        super(driver);
    }

    // Default constructor option using Singleton, consistent with BasePage.
    public ErrorPage() {
        this(DriverManager.getDriver());
    }

    /**
     * Checks whether the browser is currently on this error page.
     *
     * @return {@code true} if the current URL contains {@link #PATH}
     */
    public boolean isAtPage() {
        return isAt(PATH);
    }
}
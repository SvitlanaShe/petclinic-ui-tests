package com.adesso.qa.pages;

import org.openqa.selenium.WebDriver;

public class ErrorPage extends BasePage {
    public static final String PATH = "/oups";

    public ErrorPage(WebDriver driver) {
        super(driver);
    }

    public boolean isAtPage() {
        return isAt(PATH);
    }
}
package com.adesso.qa.pages;

import org.openqa.selenium.WebDriver;

public class HomePage extends BasePage {
    public static final String PATH = "/";

    public HomePage(WebDriver driver) {
        super(driver);
    }

    public boolean isAtPage() {
        return isAt(PATH);
    }
}
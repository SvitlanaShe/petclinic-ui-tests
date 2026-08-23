package com.adesso.qa.pages;

import org.openqa.selenium.WebDriver;

public class VetsPage extends BasePage {
    public static final String PATH = "/vets.html";

    public VetsPage(WebDriver driver) {
        super(driver);
    }

    public boolean isAtPage() {
        return isAt(PATH);
    }
}
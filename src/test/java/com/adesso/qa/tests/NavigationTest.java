package com.adesso.qa.tests;

import com.adesso.qa.pages.*;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class NavigationTest extends BaseTest {

    @Test
    void testHeaderNavigationLinks() {
        // Start at base landing page
        driver.get(BASE_URL);
        HomePage homePage = new HomePage(driver);

        // 1. Navigate to Find Owners
        FindOwnersPage findOwnersPage = homePage.clickFindOwnersMenu();
        assertThat(findOwnersPage.isAtPage()).isTrue();

        // 2. Navigate to Vets from Find Owners Page
        VetsPage vetsPage = findOwnersPage.clickVeterinariansMenu();
        assertThat(vetsPage.isAtPage()).isTrue();

        // 3. Return Home from Vets Page
        homePage = vetsPage.clickHomeMenu();
        assertThat(homePage.isAtPage()).isTrue();
    }
}
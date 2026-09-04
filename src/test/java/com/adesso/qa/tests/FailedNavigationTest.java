package com.adesso.qa.tests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.adesso.qa.pages.FindOwnersPage;
import com.adesso.qa.pages.HomePage;

public class FailedNavigationTest extends BaseTest {

    private static int attemptCount = 0;

    @Test
    void testFlakyScenario() {
        attemptCount++;
        if (attemptCount < 2) {
            // Fails on attempt 1, triggering RetryExtension
            org.junit.jupiter.api.Assertions.fail("Simulated temporary failure");
        }

        // Succeeds on attempt 2
        driver.get(BASE_URL);
    }

    @Disabled("Temporarily disabling intentional failure demo")
    @Test
    @DisplayName("Navigation Header - Intentional Failure for Report Demo")
    void testHeaderNavigationLinksIntentionalFailure() {
        // Start at base landing page
        driver.get(BASE_URL);
        HomePage homePage = new HomePage(driver);

        // 1. Navigate to Find Owners (Passes)
        FindOwnersPage findOwnersPage = homePage.clickFindOwnersMenu();
        assertThat(findOwnersPage.isAtPage())
                .as("Verify driver navigated to Find Owners page")
                .isTrue();

        // 2. Navigate to Vets Page
        findOwnersPage.clickVeterinariansMenu();

        // DELIBERATE FAILURE: Asserting wrong page object on purpose
        assertThat(findOwnersPage.isAtPage())
                .as("Intentional check: Verify Find Owners page is active after navigating to Vets")
                .isTrue();
    }

    @Test
    @DisplayName("Header Title Validation - Intentional Failure for Report Demo")
    void testHeaderTitleIntentionalFailure() {
        driver.get(BASE_URL);
        String actualTitle = "Welcome";
        String expectedTitle = "PetClinic - Admin Dashboard"; // Mismatch to force failure

        // This assertion will fail and trigger your listener's failure handling
        assertEquals(expectedTitle, actualTitle, "Header title did not match expected value!");
    }

}
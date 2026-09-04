package com.adesso.qa.tests;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.adesso.qa.pages.BasePage;

public class NavigationTest extends BaseTest {

    @BeforeEach
    public void setUp() {
        super.setUp();
        driver.get(BASE_URL);
    }

    @ParameterizedTest(name = "Navigate to {0} -> Expect path: {1}")
    @CsvSource({
            "FIND_OWNERS, /owners/find",
            "VETS,        /vets",
            "HOME,        /"
    })
    @DisplayName("Verify top header navigation links route to expected URLs")
    void testHeaderNavigationLinks(String targetMenu, String expectedPath) {
        BasePage currentPage = new BasePage(driver) {
        };

        // Action: open drawer (if mobile view - this happens when the run is from GitHub) and click menu link
        clickNavMenu();

        switch (targetMenu) {
            case "FIND_OWNERS" -> currentPage.clickFindOwnersMenu();
            case "VETS" -> currentPage.clickVeterinariansMenu();
            case "HOME" -> currentPage.clickHomeMenu();
            default -> throw new IllegalArgumentException("Unknown menu: " + targetMenu);
        }

        // Assertion
        assertThat(currentPage.isAt(expectedPath))
                .as("Verify browser URL contains path '%s'", expectedPath)
                .isTrue();
    }
}
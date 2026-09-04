package com.adesso.qa.tests;

import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.adesso.qa.pages.AddOwnerPage;
import com.adesso.qa.pages.AddPetPage;
import com.adesso.qa.pages.AddVisitPage;
import com.adesso.qa.pages.FindOwnersPage;
import com.adesso.qa.pages.OwnerInformationPage;

public class PetClinicE2ETest extends BaseTest {

    @Test
    @DisplayName("a. Search for an existing owner")
    void testSearchExistingOwner() {
        driver.get(BASE_URL + FindOwnersPage.PATH);
        FindOwnersPage findOwnersPage = new FindOwnersPage(driver);
        findOwnersPage.searchOwner("Franklin");

        OwnerInformationPage infoPage = new OwnerInformationPage(driver);
        assertThat(infoPage.getOwnerName()).contains("George Franklin");
    }

    @Test
    @DisplayName("b. Add a new owner")
    void testAddNewOwner() {
        driver.get(BASE_URL + FindOwnersPage.PATH);
        FindOwnersPage findOwnersPage = new FindOwnersPage(driver);
        findOwnersPage.clickAddOwner();

        AddOwnerPage addOwnerPage = new AddOwnerPage(driver);
        String uniqueLastName = "Tester" + System.currentTimeMillis();
        addOwnerPage.createOwner("John", uniqueLastName, "123 Main St", "Dortmund", "1234567890");

        OwnerInformationPage infoPage = new OwnerInformationPage(driver);
        assertThat(infoPage.getOwnerName()).contains("John " + uniqueLastName);

        String successMessage = findOwnersPage.getSuccessMessageText();
        Assertions.assertThat(successMessage)
                .as("Verify success toast notification after owner creation")
                .containsIgnoringCase("Owner Created") // Adjust string based on exact UI text
                .isNotEmpty();

    }

    @Test
    @DisplayName("c. Add a new pet to an owner and verify duplicate name constraint")
    void testAddPetAndDuplicateValidation() {
        driver.get(BASE_URL + FindOwnersPage.PATH);
        FindOwnersPage findOwnersPage = new FindOwnersPage(driver);
        assertThat(findOwnersPage.isAtPage()).isTrue();
        findOwnersPage.searchOwner("Franklin");

        OwnerInformationPage infoPage = new OwnerInformationPage(driver);

        String petName = "Buddy" + (System.currentTimeMillis() % 1000);
        AddPetPage addPetPage = new AddPetPage(driver);

        // First creation
        infoPage.clickAddNewPet();
        addPetPage.addPet(petName, "2023-01-01", "dog");

        String successMessage = findOwnersPage.getSuccessMessageText();
        Assertions.assertThat(successMessage)
                .as("Verify success toast notification after adding the pet")
                .containsIgnoringCase("New Pet has been Added")
                .isNotEmpty();

        // Verify pet was added before trying to add duplicate
        assertThat(infoPage.hasPet(petName))
                .as("Pet should be visible on owner page before testing duplicate rule")
                .isTrue();

        // Validate duplicate pet constraint
        infoPage.clickAddNewPet();
        addPetPage.addPet(petName, "2023-01-01", "dog");
        assertThat(addPetPage.getErrorMessage()).contains("is already in use");
    }

    @Test
    @DisplayName("d. Add a new visit for a pet")
    void testAddVisitForPet() {
        // Create an isolated owner & pet dynamically so table size stays small and clean
        driver.get(BASE_URL + FindOwnersPage.PATH);
        FindOwnersPage findOwnersPage = new FindOwnersPage(driver);
        findOwnersPage.clickAddOwner();

        AddOwnerPage addOwnerPage = new AddOwnerPage(driver);
        String uniqueLastName = "Visitor" + System.currentTimeMillis();
        addOwnerPage.createOwner("Alex", uniqueLastName, "456 Side St", "Dortmund", "0987654321");

        OwnerInformationPage infoPage = new OwnerInformationPage(driver);
        infoPage.clickAddNewPet();

        AddPetPage addPetPage = new AddPetPage(driver);
        String petName = "Milo";
        addPetPage.addPet(petName, "2022-05-05", "cat");

        String successMessage = addPetPage.getSuccessMessageText();
        Assertions.assertThat(successMessage)
                .as("Verify success toast notification after adding the pet")
                .isNotEmpty()
                .containsIgnoringCase("New pet has been added");

        // Add visit to fresh pet
        infoPage.clickAddVisitForPet(petName);

        AddVisitPage addVisitPage = new AddVisitPage(driver);
        String visitNote = "Routine Checkup " + System.currentTimeMillis();
        addVisitPage.addVisit(visitNote);

        // Verify visit was recorded
        assertThat(infoPage.hasVisitDescription(visitNote))
                .as("Description '" + visitNote + "' was not found in Previous Visits table")
                .isTrue();
    }
}
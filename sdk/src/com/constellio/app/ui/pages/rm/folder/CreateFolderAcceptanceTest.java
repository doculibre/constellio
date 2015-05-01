/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.app.ui.pages.rm.folder;

import static org.assertj.core.api.Assertions.assertThat;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

import com.constellio.app.modules.rm.DemoTestRecords;
import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.constellio.app.ui.framework.components.BaseForm;
import com.constellio.app.ui.tools.RecordFormWebElement;
import com.constellio.model.services.records.RecordServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.UiTest;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;

@UiTest
public class CreateFolderAcceptanceTest extends ConstellioTest {
	RecordFormWebElement zeForm;
	RecordServices recordServices;
	ConstellioWebDriver driver;
	RMTestRecords records;

	String classificationFinder;
	String administrativeUnitFinder;
	String filingSpaceFinderA;
	String filingSpaceFinderB;
	String filingSpaceFinderC;
	String collection;

	@Before
	public void setUp()
			throws Exception {
		filingSpaceFinderA = "A";
		filingSpaceFinderB = "B";
		filingSpaceFinderC = "C";

		givenCollectionWithTitle(zeCollection, "Collection de test").withConstellioRMModule().withAllTestUsers();
		givenCollectionWithTitle("LaCollectionDeRida", "Collection d'entreprise").withConstellioRMModule().withAllTestUsers();

		recordServices = getModelLayerFactory().newRecordServices();

		records = new RMTestRecords(zeCollection).setup(getModelLayerFactory()).withFoldersAndContainersOfEveryStatus()
				.withEvents();
		new DemoTestRecords("LaCollectionDeRida").setup(getModelLayerFactory()).withFoldersAndContainersOfEveryStatus();
	}

	@Test
	public void testsInZeCollection()
			throws Exception {
		collection = zeCollection;
		classificationFinder = "X100";
		administrativeUnitFinder = "12";

		givenUserAddFolderWhenAllFieldsAreCompleteAndClickSaveThenFolderIsCreated();
		givenUserCanAccessOneFilingSpaceWhenCreateFolderThenCantChooseAnotherFilingSpace();
		whenCreateFolderThenTitleCategoryFilingSpaceAndAdministrativeUnitAreRequired();
		givenUserCanAccessSeveralFilingSpaceWhenCreateFolderThenAdministrativeUnitMatchesWithSelectedFilingSpace();
	}

	@Test
	public void testsInCollectionDeRida()
			throws Exception {
		collection = "LaCollectionDeRida";
		classificationFinder = "23 ";
		administrativeUnitFinder = "Salle B";

		givenUserAddFolderWhenAllFieldsAreCompleteAndClickSaveThenFolderIsCreated();
		givenUserCanAccessOneFilingSpaceWhenCreateFolderThenCantChooseAnotherFilingSpace();
		whenCreateFolderThenTitleCategoryFilingSpaceAndAdministrativeUnitAreRequired();
		givenUserCanAccessSeveralFilingSpaceWhenCreateFolderThenAdministrativeUnitMatchesWithSelectedFilingSpace();
	}

	@Test
	public void whenModifyingCategoryThenRetentionRuleAndCopyTypeVisibilityIsAdjusted()
			throws Exception {
		collection = zeCollection;

		navigateToAddFolderFormLoggedAs(gandalf, collection);
		assertThat(zeForm.isVisible("folder_default_categoryEntered")).isTrue();
		assertThat(zeForm.isVisible("folder_default_retentionRuleEntered")).isFalse();
		assertThat(zeForm.isVisible("folder_default_copyStatusEntered")).isFalse();

		zeForm.getDropDown("folder_default_categoryEntered").typeAndSelectFirst("X13");
		assertThat(zeForm.isVisible("folder_default_retentionRuleEntered")).isTrue();
		assertThat(zeForm.isVisible("folder_default_copyStatusEntered")).isFalse();

		zeForm.getDropDown("folder_default_retentionRuleEntered").selectItemContainingText("2");
		Thread.sleep(50);
		assertThat(zeForm.isVisible("folder_default_copyStatusEntered")).isTrue();

		zeForm.getDropDown("folder_default_retentionRuleEntered").selectItemContainingText("1");
		Thread.sleep(50);
		assertThat(zeForm.isVisible("folder_default_copyStatusEntered")).isFalse();

		zeForm.getDropDown("folder_default_categoryEntered").expandOptions().select(0).typeAndSelectFirst("X100");
		Thread.sleep(50);
		assertThat(zeForm.isVisible("folder_default_retentionRuleEntered")).isFalse();
		assertThat(zeForm.isVisible("folder_default_copyStatusEntered")).isFalse();
	}

	public void givenUserAddFolderWhenAllFieldsAreCompleteAndClickSaveThenFolderIsCreated()
			throws Exception {
		navigateToAddFolderFormLoggedAs(admin, collection);
		completeFormAndSave();

		assertThat(folderTitle()).isEqualTo("Pokemon");
	}

	public void givenUserCanAccessOneFilingSpaceWhenCreateFolderThenCantChooseAnotherFilingSpace()
			throws Exception {
		navigateToAddFolderFormLoggedAs(charlesFrancoisXavier, collection);
		completeFormAndSave();

		assertThat(folderFilingSpace()).contains("A");
		assertThat(folderAdministrativeUnit()).contains("A");
	}

	public void givenUserCanAccessSeveralFilingSpaceWhenCreateFolderThenAdministrativeUnitMatchesWithSelectedFilingSpace()
			throws Exception {
		navigateToAddFolderFormLoggedAs(gandalf, collection);

		zeForm.getDropDown("folder_default_filingSpaceEntered").selectItemContainingText(filingSpaceFinderA);
		assertThat(zeForm.getDropDown("folder_default_filingSpaceEntered").getSelectedValue()).contains("A");

		zeForm.getDropDown("folder_default_filingSpaceEntered").selectItemContainingText(filingSpaceFinderB);
		assertThat(zeForm.getDropDown("folder_default_filingSpaceEntered").getSelectedValue()).contains("B");

		zeForm.getDropDown("folder_default_filingSpaceEntered").selectItemContainingText(filingSpaceFinderC);
		assertThat(zeForm.getDropDown("folder_default_filingSpaceEntered").getSelectedValue()).contains("C");
	}

	public void whenCreateFolderThenTitleCategoryFilingSpaceAndAdministrativeUnitAreRequired()
			throws Exception {
		navigateToAddFolderFormLoggedAs(charlesFrancoisXavier, collection);
		completeNoRequiredField();
		zeForm.clickSaveButtonAndWaitForPageReload();

		assertThat(driver.getCurrentPage()).contains("addFolder");
	}

	private void navigateToAddFolderFormLoggedAs(String user, String collection) {
		driver = newWebDriver(loggedAsUserInCollection(user, collection));
		driver.navigateTo().url(NavigatorConfigurationService.ADD_FOLDER);
		zeForm = new RecordFormWebElement(driver.findElement(By.className(BaseForm.BASE_FORM)));
	}

	private void completeFormAndSave() {
		completeRequiredField();
		completeNoRequiredField();
		zeForm.clickSaveButtonAndWaitForPageReload();
	}

	public void completeRequiredField() {
		zeForm.getTextField("folder_default_title").setValue("Pokemon");

		zeForm.getLookupField("folder_default_categoryEntered").typeAndSelectFirst(classificationFinder);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		if (zeForm.isVisible("folder_default_retentionRuleEntered")) {
			zeForm.getDropDown("folder_default_retentionRuleEntered").selectItemContainingText("1");
		}
		zeForm.getDateField("folder_default_openingDate").setValue(new LocalDate(2015, 2, 21));
		zeForm.getDropDown("folder_default_filingSpaceEntered").selectItemContainingText(filingSpaceFinderB);
		zeForm.getDropDown("folder_default_administrativeUnitEntered").selectItemContainingText(administrativeUnitFinder);
	}

	private void completeNoRequiredField() {
		zeForm.getDateField("folder_default_enteredClosingDate").setValue(new LocalDate(2016, 2, 21));
		zeForm.toggleAllCheckbox();
		zeForm.getListAddRemoveTextField("folder_default_keywords").add("pokemon");
		zeForm.getTextField("folder_default_description").setValue("Attrapez les tous");
	}

	private String folderTitle() {
		return driver.findElement(By.id("display-value-folder_default_title")).getText();
	}

	private String folderAdministrativeUnit() {
		return driver.findElement(By.id("display-value-folder_default_administrativeUnit")).getText();
	}

	private String folderFilingSpace() {
		return driver.findElement(By.id("display-value-folder_default_filingSpace")).getText();
	}
}

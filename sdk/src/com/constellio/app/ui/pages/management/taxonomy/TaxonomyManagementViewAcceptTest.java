package com.constellio.app.ui.pages.management.taxonomy;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.UiTest;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;

@UiTest
public class TaxonomyManagementViewAcceptTest extends ConstellioTest {

	RecordServices recordServices;
	RMTestRecords records = new RMTestRecords(zeCollection);
	ConstellioWebDriver driver;
	TaxonomyManagementWebElement taxoManagementWebElement;

	@Before
	public void setUp()
			throws Exception {
		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers().withRMTest(records)
						.withFoldersAndContainersOfEveryStatus().withEvents()
		);
		inCollection(zeCollection).setCollectionTitleTo("Collection de test");

		recordServices = getModelLayerFactory().newRecordServices();

		driver = newWebDriver(loggedAsUserInCollection(chuckNorris, zeCollection));
		taxoManagementWebElement = new TaxonomyManagementWebElement(driver);
	}

	@Test
	public void whenAddingAndDisplayingTaxonomyConceptsThenRightInformationAddedAndDisplayed()
			throws Exception {
		// TODO Add test for new information in table for Mantis C01
		taxoManagementWebElement.navigateToClassificationPlan();

		assertThat(taxoManagementWebElement.getConceptsCodesFromTable()).containsOnly("X", "Z");

		taxoManagementWebElement.display("X");

		assertThat(taxoManagementWebElement.getCurrentConceptCode()).isEqualTo("X");
		assertThat(taxoManagementWebElement.getCurrentConceptTitle()).isEqualTo("Xe category");
		assertThat(taxoManagementWebElement.getCurrentConceptCreationDate()).isEqualTo(
				records.getCategory_X().getCreatedOn().toString("yyyy-MM-dd HH:mm:ss"));
		assertThat(taxoManagementWebElement.getConceptsCodesFromTable()).containsOnly("X100", "X13");

		taxoManagementWebElement.add()
				.setValue(Category.DEFAULT_SCHEMA + "_" + Category.CODE, "NEW")
				.setValue(Category.DEFAULT_SCHEMA + "_" + Schemas.TITLE.getLocalCode(), "New category")
				.clickSaveButtonAndWaitForPageReload();

		assertThat(taxoManagementWebElement.getCurrentConceptCode()).isEqualTo("X");
		assertThat(taxoManagementWebElement.getConceptsCodesFromTable()).containsOnly("X100", "X13", "NEW");

		taxoManagementWebElement.display("NEW");

		assertThat(taxoManagementWebElement.getCurrentConceptCode()).isEqualTo("NEW");
		assertThat(taxoManagementWebElement.getCurrentConceptTitle()).isEqualTo("New category");

		taxoManagementWebElement.add()
				.setValue(Category.DEFAULT_SCHEMA + "_" + Category.CODE, "NEWER")
				.clickCancelButtonAndWaitForPageReload();

		assertThat(taxoManagementWebElement.getConceptsCodesFromTable()).isEmpty();
	}

	@Test
	public void whenDisplayingAdministativeUnitThenRightInformationDisplayed()
			throws Exception {

		taxoManagementWebElement.navigateToAdministrativeUnit();

		assertThat(taxoManagementWebElement.getConceptsCodesFromTable()).containsOnly("10", "20", "30");

		taxoManagementWebElement.display("10");

		assertThat(taxoManagementWebElement.getCurrentAdministrativeUnitCode()).isEqualTo("10");
		assertThat(taxoManagementWebElement.getCurrentAdministrativeUnitTitle()).isEqualTo("Unité 10");
		assertThat(taxoManagementWebElement.getCurrentAdministrativeUnitCreationDate()).isEqualTo(
				records.getUnit10().getCreatedOn().toString("yyyy-MM-dd HH:mm:ss"));

		assertThat(taxoManagementWebElement.getElementByClassName("display-value-count-folder").getText())
				.isEqualTo("0");
		assertThat(taxoManagementWebElement.getConceptsCodesFromTable()).containsOnly("11", "12", "10A");

		taxoManagementWebElement.display("10A");

		assertThat(taxoManagementWebElement.getElementByClassName("display-value-count-folder").getText())
				.isEqualTo("63");
		assertThat(taxoManagementWebElement.getConceptsCodesFromTable()).isEmpty();
	}

	@Test
	public void whenDisplayingClassificationPlanThenRightInformationDisplayed()
			throws Exception {

		taxoManagementWebElement.navigateToClassificationPlan();
		taxoManagementWebElement.display("X");

		assertThat(taxoManagementWebElement.getCurrentConceptCode()).isEqualTo("X");
		assertThat(taxoManagementWebElement.getCurrentConceptTitle()).isEqualTo("Xe category");
		assertThat(taxoManagementWebElement.getCurrentConceptCreationDate()).isEqualTo(
				records.getCategory_X().getCreatedOn().toString("yyyy-MM-dd HH:mm:ss"));
		assertThat(taxoManagementWebElement.getConceptsCodesFromTable()).containsOnly("X100", "X13");
		assertThat(taxoManagementWebElement.getElementByClassName("display-value-count-folder").getText())
				.isEqualTo("0");
	}

	@Test
	public void whenDisplayingAdministativeUnitFoldersThenListOfFoldersDisplayed()
			throws Exception {

		taxoManagementWebElement.navigateToAdministrativeUnit();

		assertThat(taxoManagementWebElement.getConceptsCodesFromTable()).containsOnly("10", "20", "30");

		taxoManagementWebElement.display("30");

		assertThat(taxoManagementWebElement.getConceptsCodesFromTable()).containsOnly("30C");

		taxoManagementWebElement.display("30C");

		assertThat(taxoManagementWebElement.getElementByClassName("display-value-count-folder").getText())
				.isEqualTo("21");
		taxoManagementWebElement.getTabByClassName("folderTab").click();
		Thread.sleep(1000);

		taxoManagementWebElement.waitUntilFoldersTableExist();

		assertThat(taxoManagementWebElement.getRowsFoldersFromFoldersTable().get(1).getText()).contains("Asperge");
	}
}

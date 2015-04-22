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
	RMTestRecords records;
	ConstellioWebDriver driver;
	TaxonomyManagementWebElement taxoManagementWebElement;

	@Before
	public void setUp()
			throws Exception {
		givenCollectionWithTitle(zeCollection, "Collection de test").withConstellioRMModule().withAllTestUsers();

		recordServices = getModelLayerFactory().newRecordServices();

		records = new RMTestRecords(zeCollection).setup(getModelLayerFactory()).withFoldersAndContainersOfEveryStatus()
				.withEvents();

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
}

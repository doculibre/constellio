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
package com.constellio.app.ui.pages.rm.document;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

import com.constellio.app.modules.rm.DemoTestRecords;
import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.constellio.app.ui.framework.components.ComponentState;
import com.constellio.app.ui.tools.RecordFormWebElement;
import com.constellio.model.services.records.RecordServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.UiTest;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebElement;

@UiTest
public class DisplayDocumentBorrowingAcceptanceTest extends ConstellioTest {

	RecordFormWebElement zeForm;
	RecordServices recordServices;
	ConstellioWebDriver driver;
	DemoTestRecords records2 = new DemoTestRecords("LaCollectionDeRida");
	RMTestRecords records = new RMTestRecords(zeCollection);
	RMSchemasRecordsServices schemas;

	String idDocument;

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers().withRMTest(
						records).withFoldersAndContainersOfEveryStatus()
						.withDocumentsHavingContent(),
				withCollection("LaCollectionDeRida").withConstellioRMModule().withAllTestUsers().withRMTest(records2)
						.withFoldersAndContainersOfEveryStatus()
		);
		inCollection("LaCollectionDeRida").setCollectionTitleTo("Collection d'entreprise");
		inCollection(zeCollection).setCollectionTitleTo("Collection de test");

		recordServices = getModelLayerFactory().newRecordServices();

		idDocument = recordIdWithTitleInCollection("assurance-EmiliePoulain.odt", "LaCollectionDeRida");
	}

	@Test
	public void givenARGDUserThenBorrowingIsOk()
			throws Exception {
		logAs(admin);

		navigateToADocumentInZeCollection();
		assertThatBorrowingEnabled();

		navigateToSemiActiveDocument();
		assertThatBorrowingEnabled();

		navigateToInactiveDocument();
		assertThatBorrowingEnabled();
	}

	@Test
	public void givenAliceThenBorrowingIsNotPossible() {
		logAs(aliceWonderland);

		navigateToADocumentInZeCollection();
		assertThatBorrowingInvisible();

		navigateToSemiActiveDocument();
		assertThatBorrowingInvisible();

		navigateToInactiveDocument();
		assertThatBorrowingInvisible();
	}

	@Test
	public void givenBobWhenBorrowingThenBobCanReturn()
			throws Exception {
		logAs(bobGratton);

		navigateToADocumentInZeCollection();
		borrowTheDocument();
		assertThatReturnButtonEnabled();
	}

	@Test
	public void givenAdminWhenSomoeoneElseBorrowADocumentThenCanReturn()
			throws Exception {
		logAs(bobGratton);

		navigateToADocumentInZeCollection();
		borrowTheDocument();

		logAs(admin);

		navigateToADocumentInZeCollection();
		assertThatReturnButtonEnabled();
	}

	@Test
	public void givenAdminWhenBorrowingThenBobCantReturn()
			throws Exception {
		logAs(admin);

		navigateToADocumentInZeCollection();
		borrowTheDocument();

		logAs(bobGratton);

		navigateToADocumentInZeCollection();
		assertThatReturnButtonInvisible();
	}

	private void assertThatReturnButtonEnabled() {
		assertThat(getButtonState("Retourner")).isSameAs(ComponentState.ENABLED);
	}

	private void assertThatBorrowingEnabled() {
		assertThat(getButtonState("Emprunter")).isSameAs(ComponentState.ENABLED);
	}

	private void assertThatBorrowingInvisible() {
		assertThat(getButtonState("Emprunter")).isSameAs(ComponentState.INVISIBLE);
	}

	private void assertThatReturnButtonInvisible() {
		assertThat(getButtonState("Retourner")).isSameAs(ComponentState.INVISIBLE);
	}

	private void logAs(String user) {
		driver = newWebDriver(loggedAsUserInCollection(user, "LaCollectionDeRida"));
	}

	private void navigateToADocumentInZeCollection() {
		driver.navigateTo().url(NavigatorConfigurationService.DISPLAY_DOCUMENT + "/" + idDocument);
	}

	private void navigateToSemiActiveDocument() {
		String id = recordIdWithTitleInCollection("Grenouille.odt", zeCollection);
		driver.navigateTo().url(NavigatorConfigurationService.DISPLAY_DOCUMENT + "/" + id);
	}

	private void navigateToInactiveDocument() {
		String id = recordIdWithTitleInCollection("Lynx.odt", zeCollection);
		driver.navigateTo().url(NavigatorConfigurationService.DISPLAY_DOCUMENT + "/" + id);
	}

	private void borrowTheDocument()
			throws Exception {
		try {
			getButton("Emprunter").clickAndWaitForPageReload();
		} catch (Exception e) {
			throw new RuntimeException("Click Impossible on Borrow Button !");
		}
	}

	private ComponentState getButtonState(String buttonName) {
		if (getButton(buttonName) == null) {
			return ComponentState.INVISIBLE;
		} else if (getButton(buttonName).isEnabled()) {
			return ComponentState.ENABLED;
		} else {
			return ComponentState.DISABLED;
		}
	}

	private ConstellioWebElement getButton(String buttonName) {
		try {
			driver.waitUntilElementExist(By.className("action-menu-button"));
		} catch (Exception e) {
			return null;
		}
		List<ConstellioWebElement> listButtonActionMenu = driver.findAdaptElements(By.className("action-menu-button"));
		for (ConstellioWebElement buttonElement : listButtonActionMenu) {
			if (buttonElement.getText().contains(buttonName)) {
				return buttonElement;
			}
		}
		return null;
	}

}

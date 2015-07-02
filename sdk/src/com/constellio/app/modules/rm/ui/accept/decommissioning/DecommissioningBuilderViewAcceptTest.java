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
package com.constellio.app.modules.rm.ui.accept.decommissioning;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.decommissioning.SearchType;
import com.constellio.app.modules.rm.ui.pages.decommissioning.DecommissioningBuilderViewImpl.DecommissioningButton;
import com.constellio.app.ui.tools.ButtonWebElement;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.UiTest;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;

@UiTest
public class DecommissioningBuilderViewAcceptTest extends ConstellioTest {
	RMTestRecords records = new RMTestRecords(zeCollection);
	ConstellioWebDriver driver;
	DecommissioningBuilderPage page;

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers().withRMTest(records)
						.withFoldersAndContainersOfEveryStatus()
		);

		driver = newWebDriver(loggedAsUserInCollection(gandalf, zeCollection));
	}

	@Test
	public void givenAdministratorThenCanSelectFilingSpaceAndAdministrativeUnit() {
		givenTransfer();

		assertThat(page.getSearchButton().isEnabled()).isFalse();

		page.getFilingSpace().typeAndSelectFirst("Room A");
		assertThat(page.getAdministrativeUnit().getSelectedValue()).isEqualTo("Administrative unit with room A");
		assertThat(page.getSearchButton().isEnabled()).isTrue();

		page.getFilingSpace().clear().typeAndSelectFirst("Room B");
		assertThat(page.getAdministrativeUnit().getSelectedValue()).isNullOrEmpty();
		assertThat(page.getSearchButton().isEnabled()).isFalse();
	}

	@Test
	public void givenAdministratorThenCanCreateListToCloseWithFixedPeriod() {
		givenCloseWithFixedPeriod();
		createNewList();
	}

	@Test
	public void givenAdministratorThenCanCreateListToCloseWithCode888() {
		givenCloseWithCode888();
		createNewList();
	}

	@Test
	public void givenAdministratorThenCanCreateListToCloseWithCode999() {
		givenCloseWithCode999();
		createNewList();
	}

	@Test
	public void givenAdministratorThenCanCreateListToTransfer() {
		givenTransfer();
		createNewList();
	}

	@Test
	public void givenAdministratorThenCanCreateListOfActiveToDeposit() {
		givenActiveToDeposit();
		createNewList();
	}

	@Test
	public void givenAdministratorThenCanCreateListOfActiveToDestroy() {
		givenActiveToDestroy();
		createNewList();
	}

	@Test
	public void givenAdministratorThenCanCreateListOfSemiActiveToDeposit() {
		givenSemiActiveToDeposit();
		createNewList();
	}

	@Test
	public void givenAdministratorThenCanCreateListOfSemiActiveToDestroy() {
		givenSemiActiveToDestroy();
		createNewList();
	}

	private void givenCloseWithFixedPeriod() {
		page = new DecommissioningBuilderPage(driver).navigateToPage(SearchType.fixedPeriod);
	}

	private void givenCloseWithCode888() {
		page = new DecommissioningBuilderPage(driver).navigateToPage(SearchType.code888);
	}

	private void givenCloseWithCode999() {
		page = new DecommissioningBuilderPage(driver).navigateToPage(SearchType.code999);
	}

	private void givenTransfer() {
		page = new DecommissioningBuilderPage(driver).navigateToPage(SearchType.transfer);
	}

	private void givenActiveToDeposit() {
		page = new DecommissioningBuilderPage(driver).navigateToPage(SearchType.activeToDeposit);
	}

	private void givenActiveToDestroy() {
		page = new DecommissioningBuilderPage(driver).navigateToPage(SearchType.activeToDestroy);
	}

	private void givenSemiActiveToDeposit() {
		page = new DecommissioningBuilderPage(driver).navigateToPage(SearchType.semiActiveToDeposit);
	}

	private void givenSemiActiveToDestroy() {
		page = new DecommissioningBuilderPage(driver).navigateToPage(SearchType.semiActiveToDestroy);
	}

	private void createNewList() {
		page.getFilingSpace().typeAndSelectFirst("Room A");
		ButtonWebElement create = page.searchAndWaitForResults().getCreateButton();
		assertThat(create.isEnabled()).isFalse();

		page.getAllResultCheckBoxes().get(0).toggle();
		assertThat(create.isEnabled()).isTrue();

		page.openCreateForm()
				.setValue(DecommissioningButton.TITLE, "New list")
				.setValue(DecommissioningButton.DESCRIPTION, "A description")
				.clickSaveButtonAndWaitForPageReload();
	}
}

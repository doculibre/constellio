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
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.UiTest;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;

@UiTest
public class DecommissioningListViewAcceptTest extends ConstellioTest {
	RMTestRecords records;
	ConstellioWebDriver driver;
	DecommissioningListPage page;

	@Before
	public void setUp()
			throws Exception {
		givenCollection(zeCollection).withConstellioRMModule().withAllTestUsers();
		records = new RMTestRecords(zeCollection).setup(getModelLayerFactory()).withFoldersAndContainersOfEveryStatus();

		driver = newWebDriver(loggedAsUserInCollection(gandalf, zeCollection));
	}

	@Test
	public void givenListsOfManyStatesThenActionsAreAvailableAccordingToTheListState() {
		givenHybridListToClose();
		assertThat(page.getEditButton().isEnabled()).isTrue();
		assertThat(page.getDeleteButton().isEnabled()).isTrue();
		assertThat(page.getProcessButton().isEnabled()).isTrue();

		givenAnalogicListToTransfer();
		assertThat(page.getEditButton().isEnabled()).isTrue();
		assertThat(page.getDeleteButton().isEnabled()).isTrue();
		assertThat(page.getProcessButton().isEnabled()).isFalse();

		givenAlreadyProcessedList();
		assertThat(page.getEditButton().isEnabled()).isFalse();
		assertThat(page.getDeleteButton().isEnabled()).isFalse();
		assertThat(page.getProcessButton().isEnabled()).isFalse();
	}

	private void givenHybridListToClose() {
		page = new DecommissioningListPage(driver, records.list_03).navigateToPage();
	}

	private void givenAnalogicListToTransfer() {
		page = new DecommissioningListPage(driver, records.list_04).navigateToPage();
	}

	public void givenAlreadyProcessedList() {
		page = new DecommissioningListPage(driver, records.list_11).navigateToPage();
	}
}

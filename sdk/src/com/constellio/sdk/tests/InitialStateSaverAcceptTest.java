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
package com.constellio.sdk.tests;

import org.junit.Test;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.sdk.tests.annotations.MainTest;
import com.constellio.sdk.tests.annotations.UiTest;

@UiTest
@MainTest
public class InitialStateSaverAcceptTest extends ConstellioTest {

	@Test
	public void saveCurrentInitialState()
			throws Exception {

		givenTransactionLogIsEnabled();
		givenCollection(zeCollection).withConstellioRMModule();

		getSaveStateFeature().saveCurrentStateToInitialStatesFolder();
	}

	@Test
	public void saveModifiedState()
			throws Exception {

		givenTransactionLogIsEnabled();
		givenCollection(zeCollection).withConstellioRMModule().withAllTestUsers();

		getSaveStateFeature().saveStateAfterTestWithTitle("with_manual_modifications");

		newWebDriver(loggedAsUserInCollection(admin, zeCollection));
		waitUntilICloseTheBrowsers();

	}

	@Test
	public void saveStateWithTestRecords()
			throws Exception {

		givenTransactionLogIsEnabled();
		givenCollection(zeCollection).withConstellioRMModule().withAllTestUsers();
		RMTestRecords records = new RMTestRecords(zeCollection);
		records.setup(getModelLayerFactory()).withFoldersAndContainersOfEveryStatus();

		getSaveStateFeature().saveStateAfterTestWithTitle("with_test_records");

		newWebDriver(loggedAsUserInCollection(admin, zeCollection));
		waitUntilICloseTheBrowsers();

	}

}

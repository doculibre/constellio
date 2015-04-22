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
package com.constellio.app.ui.pages.management.schemas;

import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsMultivalue;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsSearchable;

import org.junit.Before;
import org.junit.Test;

import com.constellio.model.services.records.RecordServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.InDevelopmentTest;
import com.constellio.sdk.tests.annotations.UiTest;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;

@UiTest
@InDevelopmentTest
public class ListSchemaTypeViewAcceptTest extends ConstellioTest {

	TestsSchemasSetup setup = new TestsSchemasSetup(zeCollection);
	RecordServices recordServices;
	ConstellioWebDriver driver;

	@Before
	public void setUp()
			throws Exception {
		givenCollection(zeCollection).withAllTestUsers();

		defineSchemasManager().using(setup.withAStringMetadata(whichIsMultivalue, whichIsSearchable).withAContentMetadata(
				whichIsSearchable));

		recordServices = getModelLayerFactory().newRecordServices();
		driver = newWebDriver(loggedAsUserInCollection("admin", zeCollection) );
	}

	@Test
	public void whenNavigateToListSchemaTypePageThenWorks() {
		driver.navigateTo().url("displaySchemaType");
		waitUntilICloseTheBrowsers();
	}
}

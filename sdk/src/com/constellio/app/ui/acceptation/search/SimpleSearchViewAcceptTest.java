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
package com.constellio.app.ui.acceptation.search;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;

import com.constellio.app.entities.schemasDisplay.SchemaTypeDisplayConfig;
import com.constellio.app.entities.schemasDisplay.SchemaTypesDisplayConfig;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.InDevelopmentTest;
import com.constellio.sdk.tests.annotations.UiTest;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;

@UiTest
@InDevelopmentTest
public class SimpleSearchViewAcceptTest extends ConstellioTest {
	SimpleSearchViewAcceptTestSetup setup = new SimpleSearchViewAcceptTestSetup(zeCollection);
	ConstellioWebDriver driver;

	@Before
	public void setUp()
			throws Exception {
		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers()
		);
		inCollection(zeCollection).giveReadAccessTo(gandalf);
		defineSchemasManager().using(setup);

		SchemasDisplayManager schemasDisplayManager = getAppLayerFactory().getMetadataSchemasDisplayManager();

		schemasDisplayManager.saveTypes(new SchemaTypesDisplayConfig(
				zeCollection, Arrays.asList("fakeDocument_default_someFacet", "fakeDocument_default_anotherFacet")));
		schemasDisplayManager.saveType(new SchemaTypeDisplayConfig(
				zeCollection, "fakeDocument", Collections.<String>emptyList()).withSimpleSearchStatus(true));

		driver = newWebDriver(loggedAsUserInCollection(gandalf, zeCollection));
	}

	@Test
	public void givenSomeDocuments() {
		setup.givenRecords(getModelLayerFactory().newRecordServices());
		driver.navigateTo().url("search/text");
		waitUntilICloseTheBrowsers();
	}
}

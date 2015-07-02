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
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.constellio.app.entities.schemasDisplay.MetadataDisplayConfig;
import com.constellio.app.entities.schemasDisplay.SchemaTypeDisplayConfig;
import com.constellio.app.entities.schemasDisplay.SchemaTypesDisplayConfig;
import com.constellio.app.entities.schemasDisplay.enums.MetadataInputType;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.InDevelopmentTest;
import com.constellio.sdk.tests.annotations.UiTest;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;

@UiTest
@InDevelopmentTest
public class AdvancedSearchViewAcceptTest extends ConstellioTest {
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
		schemasDisplayManager.saveTypes(new SchemaTypesDisplayConfig(zeCollection,
				Arrays.asList("fakeDocument_default_someFacet", "fakeDocument_default_anotherFacet")));

		schemasDisplayManager.saveType(new SchemaTypeDisplayConfig(zeCollection, "fakeDocument", (List) Collections.emptyList())
				.withAdvancedSearchStatus(true));
		schemasDisplayManager.saveType(new SchemaTypeDisplayConfig(zeCollection, "user", (List) Collections.emptyList())
				.withAdvancedSearchStatus(true));

		schemasDisplayManager.saveMetadata(
				new MetadataDisplayConfig(zeCollection, "fakeDocument_default_title", true, MetadataInputType.FIELD, true,
						"default"));
		schemasDisplayManager.saveMetadata(
				new MetadataDisplayConfig(zeCollection, "fakeDocument_default_bodyText", true, MetadataInputType.FIELD, true,
						"default"));
		schemasDisplayManager.saveMetadata(
				new MetadataDisplayConfig(zeCollection, "fakeDocument_default_number", true, MetadataInputType.FIELD, true,
						"default"));
		schemasDisplayManager.saveMetadata(
				new MetadataDisplayConfig(zeCollection, "fakeDocument_default_someFacet", true, MetadataInputType.FIELD, true,
						"default"));
		schemasDisplayManager.saveMetadata(
				new MetadataDisplayConfig(zeCollection, "fakeDocument_default_anotherFacet", true, MetadataInputType.FIELD, true,
						"default"));
		schemasDisplayManager.saveMetadata(
				new MetadataDisplayConfig(zeCollection, "fakeDocument_default_date", true, MetadataInputType.FIELD, true,
						"default"));
		schemasDisplayManager.saveMetadata(
				new MetadataDisplayConfig(zeCollection, "fakeDocument_default_zenum", true, MetadataInputType.DROPDOWN, true,
						"default"));
		schemasDisplayManager.saveMetadata(
				new MetadataDisplayConfig(zeCollection, "fakeDocument_default_createdOn", true, MetadataInputType.FIELD, true,
						"default"));
		schemasDisplayManager.saveMetadata(
				new MetadataDisplayConfig(zeCollection, "fakeDocument_default_createdBy", true, MetadataInputType.LOOKUP, true,
						"default"));

		schemasDisplayManager.saveMetadata(
				new MetadataDisplayConfig(zeCollection, "user_default_username", true, MetadataInputType.FIELD, true, "default"));
		schemasDisplayManager.saveMetadata(
				new MetadataDisplayConfig(zeCollection, "user_default_firstname", true, MetadataInputType.FIELD, true,
						"default"));
		schemasDisplayManager.saveMetadata(
				new MetadataDisplayConfig(zeCollection, "user_default_lastname", true, MetadataInputType.FIELD, true, "default"));
		schemasDisplayManager.saveMetadata(
				new MetadataDisplayConfig(zeCollection, "user_default_email", true, MetadataInputType.FIELD, true, "default"));

		driver = newWebDriver(loggedAsUserInCollection(gandalf, zeCollection));
	}

	@Test
	public void givenSomeDocuments() {
		setup.givenRecords(getModelLayerFactory().newRecordServices());
		driver.navigateTo().url("");
		waitUntilICloseTheBrowsers();
	}
}

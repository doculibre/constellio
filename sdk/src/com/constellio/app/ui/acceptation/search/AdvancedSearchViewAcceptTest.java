package com.constellio.app.ui.acceptation.search;

import com.constellio.app.entities.schemasDisplay.MetadataDisplayConfig;
import com.constellio.app.entities.schemasDisplay.SchemaTypeDisplayConfig;
import com.constellio.app.entities.schemasDisplay.SchemaTypesDisplayConfig;
import com.constellio.app.entities.schemasDisplay.enums.MetadataDisplayType;
import com.constellio.app.entities.schemasDisplay.enums.MetadataInputType;
import com.constellio.app.entities.schemasDisplay.enums.MetadataSortingType;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.Language;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.InDevelopmentTest;
import com.constellio.sdk.tests.annotations.UiTest;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

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

		schemasDisplayManager.saveType(
				new SchemaTypeDisplayConfig(zeCollection, "fakeDocument", Collections.<String, Map<Language, String>>emptyMap())
						.withAdvancedSearchStatus(true));
		schemasDisplayManager
				.saveType(new SchemaTypeDisplayConfig(zeCollection, "user", Collections.<String, Map<Language, String>>emptyMap())
						.withAdvancedSearchStatus(true));

		schemasDisplayManager.saveMetadata(
				new MetadataDisplayConfig(zeCollection, "fakeDocument_default_title", true, MetadataInputType.FIELD, true,
						"default", MetadataDisplayType.VERTICAL, MetadataSortingType.ENTRY_ORDER));
		schemasDisplayManager.saveMetadata(
				new MetadataDisplayConfig(zeCollection, "fakeDocument_default_bodyText", true, MetadataInputType.FIELD, true,
						"default", MetadataDisplayType.VERTICAL, MetadataSortingType.ENTRY_ORDER));
		schemasDisplayManager.saveMetadata(
				new MetadataDisplayConfig(zeCollection, "fakeDocument_default_number", true, MetadataInputType.FIELD, true,
						"default", MetadataDisplayType.VERTICAL, MetadataSortingType.ENTRY_ORDER));
		schemasDisplayManager.saveMetadata(
				new MetadataDisplayConfig(zeCollection, "fakeDocument_default_someFacet", true, MetadataInputType.FIELD, true,
						"default", MetadataDisplayType.VERTICAL, MetadataSortingType.ENTRY_ORDER));
		schemasDisplayManager.saveMetadata(
				new MetadataDisplayConfig(zeCollection, "fakeDocument_default_anotherFacet", true, MetadataInputType.FIELD, true,
						"default", MetadataDisplayType.VERTICAL, MetadataSortingType.ENTRY_ORDER));
		schemasDisplayManager.saveMetadata(
				new MetadataDisplayConfig(zeCollection, "fakeDocument_default_date", true, MetadataInputType.FIELD, true,
						"default", MetadataDisplayType.VERTICAL, MetadataSortingType.ENTRY_ORDER));
		schemasDisplayManager.saveMetadata(
				new MetadataDisplayConfig(zeCollection, "fakeDocument_default_zenum", true, MetadataInputType.DROPDOWN, true,
						"default", MetadataDisplayType.VERTICAL, MetadataSortingType.ENTRY_ORDER));
		schemasDisplayManager.saveMetadata(
				new MetadataDisplayConfig(zeCollection, "fakeDocument_default_createdOn", true, MetadataInputType.FIELD, true,
						"default", MetadataDisplayType.VERTICAL, MetadataSortingType.ENTRY_ORDER));
		schemasDisplayManager.saveMetadata(
				new MetadataDisplayConfig(zeCollection, "fakeDocument_default_createdBy", true, MetadataInputType.LOOKUP, true,
						"default", MetadataDisplayType.VERTICAL, MetadataSortingType.ENTRY_ORDER));

		schemasDisplayManager.saveMetadata(
				new MetadataDisplayConfig(zeCollection, "user_default_username", true, MetadataInputType.FIELD, true, "default", MetadataDisplayType.VERTICAL, MetadataSortingType.ENTRY_ORDER));
		schemasDisplayManager.saveMetadata(
				new MetadataDisplayConfig(zeCollection, "user_default_firstname", true, MetadataInputType.FIELD, true,
						"default", MetadataDisplayType.VERTICAL, MetadataSortingType.ENTRY_ORDER));
		schemasDisplayManager.saveMetadata(
				new MetadataDisplayConfig(zeCollection, "user_default_lastname", true, MetadataInputType.FIELD, true, "default", MetadataDisplayType.VERTICAL, MetadataSortingType.ENTRY_ORDER));
		schemasDisplayManager.saveMetadata(
				new MetadataDisplayConfig(zeCollection, "user_default_email", true, MetadataInputType.FIELD, true, "default", MetadataDisplayType.VERTICAL, MetadataSortingType.ENTRY_ORDER));

		driver = newWebDriver(loggedAsUserInCollection(gandalf, zeCollection));
	}

	@Test
	public void givenSomeDocuments() {
		setup.givenRecords(getModelLayerFactory().newRecordServices());
		driver.navigateTo().url("");
		waitUntilICloseTheBrowsers();
	}
}

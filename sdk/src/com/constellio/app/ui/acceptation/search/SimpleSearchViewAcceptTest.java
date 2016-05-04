package com.constellio.app.ui.acceptation.search;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.constellio.app.entities.schemasDisplay.SchemaTypeDisplayConfig;
import com.constellio.app.entities.schemasDisplay.SchemaTypesDisplayConfig;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.Language;
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
				zeCollection, "fakeDocument", Collections.<String, Map<Language, String>>emptyMap())
				.withSimpleSearchStatus(true));

		driver = newWebDriver(loggedAsUserInCollection(gandalf, zeCollection));
	}

	@Test
	public void givenSomeDocuments() {
		setup.givenRecords(getModelLayerFactory().newRecordServices());
		driver.navigateTo().url("search/text");
		waitUntilICloseTheBrowsers();
	}
}

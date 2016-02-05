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

		prepareSystem(
				withZeCollection().withAllTestUsers()
		);

		defineSchemasManager().using(setup.withAStringMetadata(whichIsMultivalue, whichIsSearchable).withAContentMetadata(
				whichIsSearchable));

		recordServices = getModelLayerFactory().newRecordServices();
		driver = newWebDriver(loggedAsUserInCollection("admin", zeCollection));
	}

	@Test
	public void whenNavigateToListSchemaTypePageThenWorks() {
		driver.navigateTo().url("displaySchemaType");
		waitUntilICloseTheBrowsers();
	}
}

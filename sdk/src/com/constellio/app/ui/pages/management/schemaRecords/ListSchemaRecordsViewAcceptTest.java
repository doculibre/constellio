package com.constellio.app.ui.pages.management.schemaRecords;

import org.junit.Before;
import org.junit.Test;

import com.constellio.model.services.records.RecordServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.annotations.InDevelopmentTest;
import com.constellio.sdk.tests.annotations.UiTest;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;

@UiTest
@InDevelopmentTest
public class ListSchemaRecordsViewAcceptTest extends ConstellioTest {

	SchemaRecordsViewAcceptTestSetup setup = new SchemaRecordsViewAcceptTestSetup(zeCollection);
	RecordServices recordServices;
	ConstellioWebDriver driver;

	@Before
	public void setUp()
			throws Exception {
		givenCollection(zeCollection);

		defineSchemasManager().using(setup);
		recordServices = getModelLayerFactory().newRecordServices();

		driver = newWebDriver(FakeSessionContext.adminInCollection(zeCollection));
	}

	@Test
	public void givenSomeRecordsOfZeSchema()
			throws Exception {
		setup.givenSomeRecordsOfZeSchema(recordServices);
		driver.navigateTo().url("listSchemaRecords/zeSchema_default");
		waitUntilICloseTheBrowsers();
	}
}

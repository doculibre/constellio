package com.constellio.app.ui.pages.events;

import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.constellio.app.ui.pages.management.taxonomy.TaxonomyManagementViewAcceptTestSetup;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.Event;
import com.constellio.model.entities.records.wrappers.EventType;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;
import com.constellio.sdk.tests.annotations.InDevelopmentTest;
import com.constellio.sdk.tests.annotations.UiTest;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;
import com.constellio.sdk.tests.setups.Users;

@UiTest
@InDevelopmentTest
public class BaseEventCategoryViewAcceptanceTest extends ConstellioTest {
	ConstellioWebDriver driver;

	TaxonomyManagementViewAcceptTestSetup setup = new TaxonomyManagementViewAcceptTestSetup(zeCollection);
	private RMTestRecords records = new RMTestRecords(zeCollection);
	RMSchemasRecordsServices rm;

	LocalDateTime testDate = new LocalDateTime();
	Users users;

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers().withRMTest(records)
		);
		rm = new RMSchemasRecordsServices(zeCollection, getModelLayerFactory());

		driver = newWebDriver(FakeSessionContext.adminInCollection(zeCollection));
		users = new Users().using(getModelLayerFactory().newUserServices());
		String user = users.dakotaLIndien().getUsername();
		Transaction transaction = new Transaction();
		transaction
				.add(createDocument(user, testDate.minusDays(4)));
		transaction
				.add(createFolder(user, testDate));
		transaction
				.add(createFolder(user, testDate.minusDays(2)));
		getModelLayerFactory().newRecordServices().execute(transaction);
	}

	@Test
	public void givenEmptyClassificationTaxonomy()
			throws Exception {
		// This doesn't work because the taxonomy isn't created at that moment.
		driver.navigateTo()
				.url(NavigatorConfigurationService.EVENT_CATEGORY + "/" + EventCategory.FOLDERS_AND_DOCUMENTS_CREATION);
		waitUntilICloseTheBrowsers();
	}

	private RecordWrapper createDocument(String creatorUserName, LocalDateTime eventDate) {
		return createDocument(creatorUserName).setCreatedOn(eventDate);
	}

	private Event createDocument(String creatorUserName) {
		return rm.newEvent().setUsername(creatorUserName).setType(EventType.CREATE_DOCUMENT);
	}

	private RecordWrapper createFolder(String creatorUserName, LocalDateTime eventDate) {
		return createFolder(creatorUserName).setCreatedOn(eventDate);
	}

	private Event createFolder(String creatorUserName) {
		return rm.newEvent().setUsername(creatorUserName).setType(EventType.CREATE_FOLDER);
	}

}

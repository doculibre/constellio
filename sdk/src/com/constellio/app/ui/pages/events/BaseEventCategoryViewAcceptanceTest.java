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
	private RMTestRecords records;
	RMSchemasRecordsServices schemas;

	LocalDateTime testDate = new LocalDateTime();
	Users users;

	@Before
	public void setUp()
			throws Exception {

		givenCollection(zeCollection).withConstellioRMModule().withAllTestUsers();
		schemas = new RMSchemasRecordsServices(zeCollection, getModelLayerFactory());

		records = new RMTestRecords(zeCollection).setup(getModelLayerFactory());
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
		return schemas.newEvent().setUsername(creatorUserName).setType(EventType.CREATE_DOCUMENT);
	}

	private RecordWrapper createFolder(String creatorUserName, LocalDateTime eventDate) {
		return createFolder(creatorUserName).setCreatedOn(eventDate);
	}

	private Event createFolder(String creatorUserName) {
		return schemas.newEvent().setUsername(creatorUserName).setType(EventType.CREATE_FOLDER);
	}

}

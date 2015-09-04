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
package com.constellio.app.modules.rm.services.logging;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import com.constellio.app.modules.rm.wrappers.Document;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.model.enums.DecommissioningListType;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.events.RMEventsSearchServices;
import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Event;
import com.constellio.model.entities.records.wrappers.EventType;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.ZeSchemaMetadatas;
import com.constellio.sdk.tests.setups.Users;

public class DecommissioningLoggingServiceAcceptanceTest extends ConstellioTest {
	LocalDateTime shishOClock = new LocalDateTime().minusHours(3);
	LocalDateTime tockOClock = new LocalDateTime().minusHours(2);
	LocalDateTime teaOClock = new LocalDateTime().minusHours(1);

	TestsSchemasSetup zeCollectionSetup = new TestsSchemasSetup(zeCollection);
	ZeSchemaMetadatas zeSchema = zeCollectionSetup.new ZeSchemaMetadatas();

	Users users = new Users();

	RecordServices recordServices;
	DecommissioningLoggingService loggingServices;

	RMSchemasRecordsServices rm;
	private RMTestRecords records = new RMTestRecords(zeCollection);

	RMEventsSearchServices rmEventsSearchServices;

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTest(users).withRMTest(records)
						.withFoldersAndContainersOfEveryStatus()
		);

		inCollection(zeCollection).giveReadAccessTo(admin);

		recordServices = getModelLayerFactory().newRecordServices();
		loggingServices = new DecommissioningLoggingService(getModelLayerFactory());

		defineSchemasManager().using(zeCollectionSetup);
		Taxonomy taxonomy = Taxonomy.createPublic("taxo", "taxo", zeCollection, asList("zeSchemaType"));
		getModelLayerFactory().getTaxonomiesManager().addTaxonomy(taxonomy,
				getModelLayerFactory().getMetadataSchemasManager());

		rm = new RMSchemasRecordsServices(zeCollection, getModelLayerFactory());
		rmEventsSearchServices = new RMEventsSearchServices(getModelLayerFactory(), zeCollection);
		UserServices userServices = getModelLayerFactory().newUserServices();
		users.setUp(userServices);
		userServices.addUserToCollection(users.alice(), zeCollection);
		recordServices.add(users.aliceIn(zeCollection).setCollectionWriteAccess(true).setCollectionDeleteAccess(true)
				.getWrappedRecord());
		userServices.addUserToCollection(users.bob(), zeCollection);
		users = records.getUsers();
	}

	@Test
	public void whenFolderDepositThenEventsCreated()
			throws Exception {
		whenDecommissioningEventThenAdequateEventCreated(DecommissioningListType.FOLDERS_TO_DEPOSIT, EventType.FOLDER_DEPOSIT);
	}

	@Test
	public void whenFolderDestroyThenEventsCreated()
			throws Exception {
		whenDecommissioningEventThenAdequateEventCreated(DecommissioningListType.FOLDERS_TO_DESTROY,
				EventType.FOLDER_DESTRUCTION);
	}

	@Test
	public void whenFolderTransferThenEventsCreated()
			throws Exception {
		whenDecommissioningEventThenAdequateEventCreated(DecommissioningListType.FOLDERS_TO_TRANSFER,
				EventType.FOLDER_RELOCATION);
	}

	@Test
	public void whenDocumentTransferToPdfAThenEventCreated()
			throws Exception {
		SearchServices searchServices = getModelLayerFactory().newSearchServices();
		User bob = users.bobIn(zeCollection);
		LogicalSearchQuery findDocument = new LogicalSearchQuery(new LogicalSearchQuery(LogicalSearchQueryOperators.from(rm.documentSchemaType())
				.returnAll()).setNumberOfRows(1));
		Document document = rm.wrapDocument(searchServices.search(findDocument).get(0));
		loggingServices.logPdfAGeneration(document, bob);
		recordServices.flush();
		LogicalSearchQuery query = new LogicalSearchQuery();
		query.setCondition(
				LogicalSearchQueryOperators.from(rm.eventSchema()).where(
						rm.eventSchema().getMetadata(Event.TYPE)).isEqualTo(EventType.PDF_A_GENERATION));

		List<Record> events = searchServices.search(query);

		assertThat(events).hasSize(1);
		Event event = rm.wrapEvent(events.get(0));
		event.getUsername().contains(bob.getUsername());
		event.getRecordId().contains(document.getId());
	}

	private void whenDecommissioningEventThenAdequateEventCreated(DecommissioningListType decommissioningListType,
			String eventType) {
		DecommissioningList decommissioningList = rm.newDecommissioningList()
				.setDecommissioningListType(decommissioningListType);
		User bob = users.bobIn(zeCollection);
		loggingServices.logDecommissioning(decommissioningList, bob);
		recordServices.flush();
		LogicalSearchQuery query = new LogicalSearchQuery();
		query.setCondition(
				LogicalSearchQueryOperators.from(rm.eventSchema()).where(
						rm.eventSchema().getMetadata(Event.TYPE)).isEqualTo(eventType));
		SearchServices searchServices = getModelLayerFactory().newSearchServices();
		List<Record> events = searchServices.search(query);

		assertThat(events).hasSize(1);
		Event event = rm.wrapEvent(events.get(0));
		event.getUsername().contains(bob.getUsername());
	}

}

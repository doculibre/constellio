package com.constellio.model.services.logging;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.events.RMEventsSearchServices;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.Authorization;
import com.constellio.model.entities.records.wrappers.Event;
import com.constellio.model.entities.records.wrappers.EventType;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.TestRecord;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.ZeSchemaMetadatas;
import com.constellio.sdk.tests.setups.Users;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

public class LoggingServicesAcceptTest extends ConstellioTest {

	LocalDateTime shishOClock = new LocalDateTime().minusHours(3);
	LocalDateTime tockOClock = new LocalDateTime().minusHours(2);
	LocalDateTime teaOClock = new LocalDateTime().minusHours(1);

	TestsSchemasSetup zeCollectionSetup = new TestsSchemasSetup(zeCollection);
	ZeSchemaMetadatas zeSchema = zeCollectionSetup.new ZeSchemaMetadatas();

	Users users = new Users();

	RecordServices recordServices;
	LoggingServices loggingServices;

	RMSchemasRecordsServices rm;
	private RMTestRecords records = new RMTestRecords(zeCollection);

	RMEventsSearchServices rmEventsSearchServices;
	SearchServices searchServices;
	private User alice;

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(
				withZeCollection().withAllTestUsers().withConstellioRMModule().withRMTest(records)
						.withFoldersAndContainersOfEveryStatus()
		);
		inCollection(zeCollection).giveReadAccessTo(admin);

		recordServices = getModelLayerFactory().newRecordServices();
		loggingServices = getModelLayerFactory().newLoggingServices();

		defineSchemasManager().using(zeCollectionSetup);

		Map<Language, String> labelTitle1 = new HashMap<>();
		labelTitle1.put(Language.French, "taxo");

		Taxonomy taxonomy = Taxonomy.createPublic("taxo", labelTitle1, zeCollection, asList("zeSchemaType"));
		getModelLayerFactory().getTaxonomiesManager().addTaxonomy(taxonomy,
				getModelLayerFactory().getMetadataSchemasManager());

		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		rmEventsSearchServices = new RMEventsSearchServices(getModelLayerFactory(), zeCollection);
		searchServices = getModelLayerFactory().newSearchServices();
		UserServices userServices = getModelLayerFactory().newUserServices();
		users.setUp(userServices);
		userServices.addUserToCollection(users.charles(), zeCollection);
		userServices.addUserToCollection(users.alice(), zeCollection);
		recordServices.add(users.aliceIn(zeCollection).setCollectionWriteAccess(true).setCollectionDeleteAccess(true)
				.getWrappedRecord());
		userServices.addUserToCollection(users.bob(), zeCollection);
		users = records.getUsers();
		alice = users.aliceIn(zeCollection);
	}

	@Test
	public void whenSavingNewRecordsThenEventsCreated_run1()
			throws Exception {
		whenSavingNewRecordsThenEventsCreated();
	}

	@Test
	public void whenSavingNewRecordsThenEventsCreated_run2()
			throws Exception {
		whenSavingNewRecordsThenEventsCreated();
	}

	@Test
	public void whenSavingNewRecordsThenEventsCreated_run3()
			throws Exception {
		whenSavingNewRecordsThenEventsCreated();
	}

	@Test
	public void whenSavingNewRecordsThenEventsCreated_run4()
			throws Exception {
		whenSavingNewRecordsThenEventsCreated();
	}

	@Test
	public void whenSavingNewRecordsThenEventsCreated_run5()
			throws Exception {
		whenSavingNewRecordsThenEventsCreated();
	}

	/*//TODO

	@Test
	public void whenGrantPermissionThenEventsCreated()
			throws Exception {
		assertThat(getEventSize()).isEqualTo(0);
		Authorization permission = newAuthorization("MANAGER", Arrays.asList(users.bobIn(zeCollection)), Arrays.asList(records.getFolder_A01().getWrappedRecord()));
		loggingServices.grantPermission(permission, alice);
		recordServices.flush();

	}


	@Test
	public void whenDeletePermissionThenEventsCreated()
			throws Exception {
		assertThat(getEventSize()).isEqualTo(0);
	}

	@Test
	public void whenModifyPermissionThenEventsCreated()
			throws Exception {
		assertThat(getEventSize()).isEqualTo(0);
	}

	@Test
	public void whenUserLoginThenEventCreated()
			throws Exception {
		assertThat(getEventSize()).isEqualTo(0);
	}

	@Test
	public void whenUserLogoutThenEventCreated()
			throws Exception {
		assertThat(getEventSize()).isEqualTo(0);
	}

	@Test
	public void whenBorrowRecordThenEventCreated()
			throws Exception {
		whenBorrowFolderThenEventCreated();
		whenBorrowDocumentThenEventCreated();
		whenBorrowContainerThenEventCreated();
	}

	@Test
	public void whenReturnRecordThenEventCreated()
			throws Exception {
		whenReturnFolderThenEventCreated();
		whenReturnDocumentThenEventCreated();
		whenReturnContainerThenEventCreated();
	}

	@Test
	public void whenviewRecordThenEventCreated()
			throws Exception {
		whenBorrowFolderThenEventCreated();
		whenBorrowDocumentThenEventCreated();
		whenBorrowContainerThenEventCreated();
	}

	@Test
	public void whenAddUserThenEventCreated()
			throws Exception {
		assertThat(getEventSize()).isEqualTo(0);

	}

	@Test
	public void whenRemoveUserThenEventCreated()
			throws Exception {

	}

	@Test
	public void whenAddGroupThenEventCreated()
			throws Exception {
		assertThat(getEventSize()).isEqualTo(0);

	}

	@Test
	public void whenRemoveGroupThenEventCreated()
			throws Exception {
		assertThat(getEventSize()).isEqualTo(0);

	}*/

	//TODO Francis Server integration Build 134
	private void whenSavingNewRecordsThenEventsCreated()
			throws Exception {

		Transaction transaction = new Transaction();
		transaction.add(new TestRecord(zeSchema, "recordAddedWithoutUserForWhichNoEventIsCreated"));
		recordServices.execute(transaction);

		givenTimeIs(shishOClock);
		Record record2 = new TestRecord(zeSchema, "record2");
		transaction = new Transaction().setUser(users.aliceIn(zeCollection));
		transaction.add(record2);
		transaction.add(new TestRecord(zeSchema, "record1"));
		recordServices.execute(transaction);

		givenTimeIs(tockOClock);
		transaction = new Transaction().setUser(users.aliceIn(zeCollection));
		transaction.add(new TestRecord(zeSchema, "record3"));
		transaction.add(record2.set(Schemas.TITLE, "2"));
		recordServices.execute(transaction);

		givenTimeIs(teaOClock);
		recordServices.logicallyDelete(record2, users.aliceIn(zeCollection));
		recordServices.flush();

		List<Event> events = getAllEvents();

		assertThat(events).extracting("recordId", "type").containsOnly(
				tuple("record1", "create_zeSchemaType"),
				tuple("record2", "create_zeSchemaType"),
				tuple("record2", "modify_zeSchemaType"),
				tuple("record3", "create_zeSchemaType"),
				tuple("record2", "delete_zeSchemaType")
		);

		assertThat(events).hasSize(5);
		assertThat(events.get(0).getCreatedOn()).isEqualTo(shishOClock);
		assertThat(events.get(0).getCollection()).isEqualTo(zeCollection);
		assertThat(events.get(0).getUsername()).isEqualTo(aliceWonderland);
		assertThat(events.get(0).getRecordId()).isEqualTo("record1");
		assertThat(events.get(0).getType()).isEqualTo("create_zeSchemaType");

		assertThat(events.get(1).getCreatedOn()).isEqualTo(shishOClock);
		assertThat(events.get(1).getCollection()).isEqualTo(zeCollection);
		assertThat(events.get(1).getUsername()).isEqualTo(aliceWonderland);
		assertThat(events.get(1).getRecordId()).isEqualTo("record2");
		assertThat(events.get(1).getType()).isEqualTo("create_zeSchemaType");

		assertThat(events.get(2).getCreatedOn()).isEqualTo(tockOClock);
		assertThat(events.get(2).getCollection()).isEqualTo(zeCollection);
		assertThat(events.get(2).getUsername()).isEqualTo(aliceWonderland);
		assertThat(events.get(2).getRecordId()).isEqualTo("record2");
		assertThat(events.get(2).getType()).isEqualTo("modify_zeSchemaType");

		assertThat(events.get(3).getCreatedOn()).isEqualTo(tockOClock);
		assertThat(events.get(3).getCollection()).isEqualTo(zeCollection);
		assertThat(events.get(3).getUsername()).isEqualTo(aliceWonderland);
		assertThat(events.get(3).getRecordId()).isEqualTo("record3");
		assertThat(events.get(3).getType()).isEqualTo("create_zeSchemaType");

		assertThat(events.get(4).getCreatedOn()).isEqualTo(teaOClock);
		assertThat(events.get(4).getCollection()).isEqualTo(zeCollection);
		assertThat(events.get(4).getUsername()).isEqualTo(aliceWonderland);
		assertThat(events.get(4).getRecordId()).isEqualTo("record2");
		assertThat(events.get(4).getType()).isEqualTo("delete_zeSchemaType");

	}

	private List<Event> getAllEvents() {
		SearchServices searchServices = getModelLayerFactory().newSearchServices();
		SchemasRecordsServices schemas = new SchemasRecordsServices(zeCollection, getModelLayerFactory());

		LogicalSearchCondition condition = LogicalSearchQueryOperators.from(schemas.eventSchema()).returnAll();
		LogicalSearchQuery query = new LogicalSearchQuery(condition);
		query.sortAsc(Schemas.CREATED_ON).sortAsc(Schemas.IDENTIFIER);
		return schemas.wrapEvents(searchServices.search(query));

	}

	@Test
	public void whenGrantPermissionThenLogValidEvents()
			throws Exception {

		Authorization authorization = newAuthorization("MANAGER", Arrays.asList(users.bobIn(zeCollection)),
				records.getFolder_A01().getWrappedRecord());
		User alice = users.aliceIn(zeCollection);
		loggingServices.grantPermission(authorization, alice);
		recordServices.flush();

		LogicalSearchQuery query = new LogicalSearchQuery();
		query.setCondition(
				LogicalSearchQueryOperators.from(rm.eventSchema()).where(
						rm.eventSchema().getMetadata(Event.TYPE)).isEqualTo(EventType.GRANT_PERMISSION_FOLDER));
		List<Record> events = searchServices.search(query);

		assertThat(events).hasSize(1);
		Event event = rm.wrapEvent(events.get(0));
		assertThat(event.getType()).isEqualTo(EventType.GRANT_PERMISSION_FOLDER);
	}

	private Authorization newAuthorization(String role, List<User> users, Record record) {
		List<String> roles = new ArrayList<>();
		String zRole = role;
		roles.add(zRole);
		LocalDate startDate = new LocalDate();
		LocalDate endDate = new LocalDate();
		SchemasRecordsServices schemas = new SchemasRecordsServices(zeCollection, getModelLayerFactory());

		Authorization detail = schemas.newSolrAuthorizationDetails().setRoles(roles).setStartDate(startDate)
				.setEndDate(endDate).setTarget(record.getId());
		List<String> grantedToPrincipals = new ArrayList<>();
		for (User user : users) {
			grantedToPrincipals.add(user.getId());
		}
		detail.setPrincipals(grantedToPrincipals);
		return detail;
	}

	@Test
	public void whenDeletePermissionThenReturnValidEvents()
			throws Exception {

	}

	@Test
	public void whenCreateFolderThenCreateValidEvent()
			throws Exception {

		Folder folder = rm.newFolder().setTitle("Ze Folder").setRetentionRuleEntered(records.ruleId_1)
				.setAdministrativeUnitEntered(records.unitId_10a)
				.setCategoryEntered(records.categoryId_X110).setOpenDate(new LocalDate(2010, 4, 4));
		User alice = users.aliceIn(zeCollection);

		recordServices.add(folder.getWrappedRecord(), alice);

		recordServices.flush();

		LogicalSearchQuery query = new LogicalSearchQuery();
		query.setCondition(
				LogicalSearchQueryOperators.from(rm.eventSchema()).where(
						rm.eventSchema().getMetadata(Event.TYPE)).isEqualTo(EventType.CREATE_FOLDER));
		SearchServices searchServices = getModelLayerFactory().newSearchServices();
		List<Record> folders = searchServices.search(query);
		assertThat(folders.size()).isEqualTo(1);
		Event event = rm.wrapEvent(folders.get(0));
		assertThat(event.getType()).isEqualTo(EventType.CREATE_FOLDER);
		assertThat(event.getEventPrincipalPath()).isEqualTo(folder.getWrappedRecord().get(Schemas.PRINCIPAL_PATH));
	}

	@Test
	public void whenModifyFolderThenCreateValidEvent()
			throws Exception {

		Folder folder = rm.newFolder().setRetentionRuleEntered(records.ruleId_1)
				.setAdministrativeUnitEntered(records.unitId_10a).setCategoryEntered(records.categoryId_X110).setTitle("titre1")
				.setOpenDate(new LocalDate(2010, 1, 1));
		User alice = users.aliceIn(zeCollection);

		recordServices.add(folder.getWrappedRecord(), alice);

		recordServices.flush();

		Transaction transaction = new Transaction(folder.getWrappedRecord());
		transaction.setUser(alice);
		folder.setTitle("titre2");
		loggingServices.logTransaction(transaction);
		recordServices.flush();

		LogicalSearchQuery query = new LogicalSearchQuery();
		query.setCondition(
				LogicalSearchQueryOperators.from(rm.eventSchema()).where(
						rm.eventSchema().getMetadata(Event.TYPE)).isEqualTo(EventType.MODIFY_FOLDER));
		SearchServices searchServices = getModelLayerFactory().newSearchServices();
		List<Record> folders = searchServices.search(query);
		assertThat(folders.size()).isEqualTo(1);
		Event event = rm.wrapEvent(folders.get(0));
		assertThat(event.getType()).isEqualTo(EventType.MODIFY_FOLDER);
		assertThat(event.getEventPrincipalPath()).isEqualTo(folder.getWrappedRecord().get(Schemas.PRINCIPAL_PATH));
		String expectedDelta = "[ folder_default_title :\n" +
							   "\tAvant : titre1\n" +
							   "\tAprès : titre2]\n";
		assertThat(event.getDelta()).isEqualTo(expectedDelta);
	}

	@Test
	public void whenUserLoginThenCreateValidEvent()
			throws Exception {
		User alice = users.aliceIn(zeCollection);
		loggingServices.login(alice);
		recordServices.flush();
		LogicalSearchQuery query = new LogicalSearchQuery();
		query.setCondition(
				LogicalSearchQueryOperators.from(rm.eventSchema()).where(
						rm.eventSchema().getMetadata(Event.TYPE)).isEqualTo(EventType.OPEN_SESSION));
		SearchServices searchServices = getModelLayerFactory().newSearchServices();
		List<Record> folders = searchServices.search(query);
		assertThat(folders.size()).isEqualTo(1);
		Event event = rm.wrapEvent(folders.get(0));
		assertThat(event.getType()).isEqualTo(EventType.OPEN_SESSION);
		assertThat(event.getUsername()).isEqualTo(alice.getUsername());
	}

	@Test
	public void whenBorrowOrReturnThenCreateValidEvent()
			throws Exception {
		Folder folderA02 = records.getFolder_A02();
		Folder folderBorrowedByDakota = records.getFolder_A03();
		User alice = users.aliceIn(zeCollection);
		loggingServices.borrowRecord(folderA02.getWrappedRecord(), alice);
		loggingServices.returnRecord(folderA02.getWrappedRecord(), alice);
		User charles = users.charlesIn(zeCollection);
		loggingServices.borrowRecord(folderBorrowedByDakota.getWrappedRecord(), charles);
		recordServices.flush();

		LogicalSearchQuery query = new LogicalSearchQuery();
		query.setCondition(
				LogicalSearchQueryOperators.from(rm.eventSchema()).where(
						rm.eventSchema().getMetadata(Event.TYPE)).isEqualTo(EventType.BORROW_FOLDER));
		SearchServices searchServices = getModelLayerFactory().newSearchServices();
		long borrowEventsCount = searchServices.getResultsCount(query);

		assertThat(borrowEventsCount).isEqualTo(2l);
		query = new LogicalSearchQuery();
		query.setCondition(
				LogicalSearchQueryOperators.from(rm.eventSchema()).where(
						rm.eventSchema().getMetadata(Event.TYPE)).isEqualTo(EventType.RETURN_FOLDER));
		List<Record> events = searchServices.search(query);

		assertThat(events).hasSize(1);
		Event event = rm.wrapEvent(events.get(0));
		event.getUsername().contains(alice.getUsername());
	}

	private int getEventSize() {
		return getAllEvents().size();
	}

}

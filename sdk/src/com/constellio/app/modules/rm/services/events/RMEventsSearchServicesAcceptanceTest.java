package com.constellio.app.modules.rm.services.events;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.borrowingServices.BorrowingServices;
import com.constellio.app.modules.rm.services.borrowingServices.BorrowingType;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.Event;
import com.constellio.model.entities.records.wrappers.EventType;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.logging.LoggingServices;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.users.SystemWideUserInfos;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class RMEventsSearchServicesAcceptanceTest extends ConstellioTest {

	LocalDateTime testDate = new LocalDateTime().minusMonths(1);

	Users users = new Users();

	RMSchemasRecordsServices rm;

	SearchServices searchServices;

	RMEventsSearchServices services;

	RecordServices recordServices;

	BorrowingServices borrowingServices;
	RMTestRecords records = new RMTestRecords(zeCollection);

	LocalDateTime nowDateTime = TimeProvider.getLocalDateTime();

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTest(users).withRMTest(records)
						.withFoldersAndContainersOfEveryStatus()
		);

		inCollection(zeCollection).giveReadAccessTo(admin);

		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		searchServices = getModelLayerFactory().newSearchServices();
		borrowingServices = new BorrowingServices(zeCollection, getModelLayerFactory());
		services = new RMEventsSearchServices(getModelLayerFactory(), zeCollection);
		recordServices = getModelLayerFactory().newRecordServices();
	}

	//by user
	@Test
	public void whenGetCreatedDocumentsByUserThenReturnValidEvents()
			throws Exception {

		Transaction transaction = new Transaction();
		SystemWideUserInfos user1 = users.dakotaLIndien();
		RecordWrapper documentCreationByUser1 = createDocument(user1.getUsername()).setRecordId("42").setCreatedOn(testDate);
		String expectedId = transaction
				.add(documentCreationByUser1).getId();
		SystemWideUserInfos user2 = users.chuckNorris();
		Event documentCreationByUser2 = createDocument(user2.getUsername()).setRecordId("43");
		transaction.add(documentCreationByUser2);

		getModelLayerFactory().newRecordServices().execute(transaction);

		LogicalSearchQuery query = services
				.newFindCreatedDocumentsByDateRangeAndByUserQuery(users.adminIn(zeCollection), testDate,
						testDate.plusDays(1), users.dakotaLIndienIn(zeCollection).getUsername());

		List<Event> events = rm.wrapEvents(searchServices.search(query));
		assertThat(events).hasSize(1);
		assertThat(events.get(0).getId()).isEqualTo(expectedId);
		assertThat(events.get(0).getRecordId()).isEqualTo("42");
	}

	//by date range

	@Test
	public void whenGetDeletedDocumentsByDateRangeThenReturnValidEvents()
			throws Exception {
		String user = users.dakotaLIndien().getUsername();
		Transaction transaction = new Transaction();
		transaction
				.add(deleteDocument(user, testDate.minusDays(1)));
		String expectedId = transaction
				.add(deleteDocument(user, testDate)).getId();
		transaction
				.add(deleteFolder(user, testDate));
		transaction
				.add(createDocument(user, testDate));
		getModelLayerFactory().newRecordServices().execute(transaction);

		LogicalSearchQuery query = services
				.newFindCreatedDocumentsByDateRangeQuery(users.adminIn(zeCollection), testDate, new LocalDateTime());

		List<Event> events = rm.wrapEvents(searchServices.search(query));
		assertThat(events.size()).isEqualTo(1);
		assertThat(events.get(0).getId().equals(expectedId));
	}

	//by folder
	@Test
	public void whenGetGrantedPermissionsByFolderThenReturnValidEvents()
			throws Exception {
		Folder folder1 = rm.newFolder();
		Folder folder2 = rm.newFolder();
		Transaction transaction = new Transaction();
		String expectedId = transaction
				.add(grantPermission(folder1)).getId();
		transaction
				.add(grantPermission(folder2));
		getModelLayerFactory().newRecordServices().execute(transaction);

		LogicalSearchQuery query = services
				.newFindGrantedPermissionsByDateRangeAndByFolderQuery(users.adminIn(zeCollection), testDate, new LocalDateTime(),
						folder1);

		List<Event> events = rm.wrapEvents(searchServices.search(query));
		assertThat(events).hasSize(1);
		assertThat(events.get(0).getId().equals(expectedId));
	}

	//not cancelled events
	@Test
	public void whenFindCurrentlyBorrowedFoldersThenReturnValidEvents()
			throws Exception {
		User dakota = users.dakotaLIndienIn(zeCollection);
		LoggingServices loggingServices = getModelLayerFactory().newLoggingServices();
		loggingServices.borrowRecord(records.getFolder_A01().getWrappedRecord(), dakota);
		loggingServices.returnRecord(records.getFolder_A01().getWrappedRecord(), dakota);
		User charles = users.charlesIn(zeCollection);
		loggingServices.borrowRecord(records.getFolder_A02().getWrappedRecord(), charles);
		getModelLayerFactory().newRecordServices().flush();
		List<Event> borrowedFoldersEvents = services
				.findCurrentlyBorrowedFolders(records.getAdmin());
		assertThat(borrowedFoldersEvents.size()).isEqualTo(1);
		Event event = borrowedFoldersEvents.get(0);
		assertThat(event.getUsername()).isEqualTo(charles.getUsername());
	}

	//not cancelled events
	@Test
	public void whenFindLoggedUsersThenReturnValidEvents()
			throws Exception {
		String loggedUser1Name = users.dakotaLIndien().getUsername();

		Transaction transaction = new Transaction();
		transaction
				.add(openSession(loggedUser1Name, testDate));
		transaction
				.add(closeSession(loggedUser1Name, testDate.plusDays(1)));
		transaction
				.add(openSession(loggedUser1Name, testDate.plusDays(2)));

		LocalDateTime expectedOpenSessionDate = testDate.plusDays(2);

		getModelLayerFactory().newRecordServices().execute(transaction);

		List<Event> loggedUsers = services
				.findLoggedUsers(users.adminIn(zeCollection));

		assertThat(loggedUsers).hasSize(1);
		assertThat(loggedUsers.get(0).getCreatedOn()).isEqualTo(expectedOpenSessionDate);

		String loggedUser2Name = users.alice().getUsername();
		String unloggedUserName = users.charles().getUsername();

		transaction
				.add(openSession(loggedUser2Name, testDate.plusDays(2)));

		transaction
				.add(openSession(unloggedUserName, testDate));
		transaction
				.add(closeSession(unloggedUserName, testDate.plusMinutes(1)));
		getModelLayerFactory().newRecordServices().execute(transaction);

		loggedUsers = services
				.findLoggedUsers(users.adminIn(zeCollection));

		assertThat(loggedUsers).hasSize(2);
		for (Event event : loggedUsers) {
			LocalDateTime currentDate = event.getCreatedOn();
			assertThat(currentDate).isEqualTo(expectedOpenSessionDate);
			String currentUsername = event.getUsername();
			assertThat(currentUsername).isIn(loggedUser1Name, loggedUser2Name);
		}
	}

	//by filing space
	//TODO Nouha : Le test utilise des path contenant plusieurs fois le même id, ce n'est pas possible
	//@Test
	public void whenGetCreatedFoldersByFilingSpaceThenReturnValidEvents()
			throws Exception {
		//saved paths null and /a/b/c/a/b /jk
		//yes paths to look for /a/b/c/a/b /a/b/c/a/b/ /a/b/c
		//no paths to look for /c /ab /b null
		String filingSpacePath1 = "/a/b/c/a/b";
		String filingSpacePath2 = null;
		String filingSpacePath3 = "/jk";

		Transaction transaction = new Transaction();
		transaction
				.add(createFolderInFilingSpace(filingSpacePath1).setCreatedOn(testDate));
		transaction
				.add(createFolderInFilingSpace(filingSpacePath2).setCreatedOn(testDate));
		transaction
				.add(createFolderInFilingSpace(filingSpacePath3).setCreatedOn(testDate));
		getModelLayerFactory().newRecordServices().execute(transaction);

		String[] nonAcceptedPaths = {"/c", "/ab", "/b", "/a/b/c/a/b/c"};
		LogicalSearchQuery createdFoldersInFilingSpaceQuery;
		for (String currentNonAcceptedPath : nonAcceptedPaths) {
			createdFoldersInFilingSpaceQuery = services
					.newFindCreatedFoldersByDateRangeAndByFilingSpaceQuery(users.adminIn(zeCollection), testDate,
							new LocalDateTime(), currentNonAcceptedPath);
			long count = searchServices.getResultsCount(createdFoldersInFilingSpaceQuery);
			assertThat(count).isEqualTo(0l);
		}

		String[] acceptedPaths = {"/a/b/c/a/b", "/a/b/c/a/b/", "/a/b/c"};
		for (String currentNonAcceptedPath : acceptedPaths) {
			createdFoldersInFilingSpaceQuery = services
					.newFindCreatedFoldersByDateRangeAndByFilingSpaceQuery(users.adminIn(zeCollection), testDate,
							new LocalDateTime(), currentNonAcceptedPath);
			List<Event> events = rm.wrapEvents(searchServices.search(createdFoldersInFilingSpaceQuery));
			assertThat(events.size()).isEqualTo(1);
			assertThat(events.get(0).getEventPrincipalPath()).isEqualTo(filingSpacePath1);
		}
	}

	//Current and late borrowed folders

	@Test
	public void givenBorrowedFolderByAliceWhenFindCurrentlyBorrowedFoldersByHerThenOk()
			throws Exception {

		LocalDate previewReturnDate = nowDateTime.plusDays(1).toLocalDate();
		borrowingServices
				.borrowFolder(records.getFolder_C30().getId(), nowDateTime.toLocalDate(), previewReturnDate, records.getAdmin(),
						records.getAlice(),
						BorrowingType.BORROW, true);

		LogicalSearchQuery query = services
				.newFindCurrentlyBorrowedFoldersByUser(records.getAdmin(), records.getAlice().getId());
		List<Record> records = searchServices.search(query);
		assertThat(records.size()).isEqualTo(1);
		assertThat(records.get(0).getId()).isEqualTo("C30");

	}

	@Test
	public void givenBorrowedFolderByAliceWhenFindCurrentlyBorrowedFoldersByEdouardThenNoRecordsFound()
			throws Exception {

		LocalDate previewReturnDate = nowDateTime.plusDays(1).toLocalDate();
		borrowingServices
				.borrowFolder(records.getFolder_C30().getId(), nowDateTime.toLocalDate(), previewReturnDate, records.getAdmin(),
						records.getAlice(),
						BorrowingType.BORROW, true);

		LogicalSearchQuery query = services
				.newFindCurrentlyBorrowedFoldersByUser(records.getAdmin(),
						records.getEdouard_managerInB_userInC().getId());
		List<Record> records = searchServices.search(query);
		assertThat(records.size()).isEqualTo(0);
	}

	@Test
	public void givenLateBorrowedFolderByAliceWhenFindLateBorrowedFoldersByHerThenOk()
			throws Exception {

		givenTimeIs(nowDateTime);
		LocalDate previewReturnDate = nowDateTime.plusDays(1).toLocalDate();
		borrowingServices
				.borrowFolder(records.getFolder_C30().getId(), nowDateTime.toLocalDate(), previewReturnDate, records.getAdmin(),
						records.getAlice(),
						BorrowingType.BORROW, true);

		nowDateTime = nowDateTime.plusDays(2);
		givenTimeIs(nowDateTime);

		LogicalSearchQuery query = services
				.newFindLateBorrowedFoldersByUserAndDateRangeQuery(records.getAdmin(), records.getAlice().getId());
		List<Record> records = searchServices.search(query);
		assertThat(records.size()).isEqualTo(1);
		assertThat(records.get(0).getId()).isEqualTo("C30");
	}

	@Test
	public void givenLateBorrowedFolderByAliceWhenFindLateBorrowedFoldersByEdouardThenNoRecordsFound()
			throws Exception {

		givenTimeIs(nowDateTime);
		LocalDate previewReturnDate = nowDateTime.plusDays(1).toLocalDate();
		borrowingServices
				.borrowFolder(records.getFolder_C30().getId(), nowDateTime.toLocalDate(), previewReturnDate, records.getAdmin(),
						records.getAlice(),
						BorrowingType.BORROW, true);

		nowDateTime = nowDateTime.plusDays(2);
		givenTimeIs(nowDateTime);

		LogicalSearchQuery query = services
				.newFindLateBorrowedFoldersByUserAndDateRangeQuery(records.getAdmin(),
						records.getEdouard_managerInB_userInC().getId());
		List<Record> records = searchServices.search(query);
		assertThat(records.size()).isEqualTo(0);
	}

	@Test
	public void givenBorrowedFolderWithAPastDateWhenFindBorrowedFoldersThenEventHasAPastDate()
			throws Exception {

		givenTimeIs(nowDateTime);
		LocalDate borrowingDate = nowDateTime.minusDays(10).toLocalDate();
		LocalDate previewReturnDate = borrowingDate.plusDays(2);
		borrowingServices.borrowFolder(records.folder_C30, borrowingDate, previewReturnDate,
				records.getChuckNorris(), records.getChuckNorris(), BorrowingType.BORROW, true);

		recordServices.flush();
		LogicalSearchQuery query = services
				.newFindBorrowedFoldersByDateRangeQuery(records.getChuckNorris(),
						borrowingDate.toDateTimeAtStartOfDay().toLocalDateTime(), nowDateTime);
		List<Record> records = searchServices.search(query);
		List<Event> events = rm.wrapEvents(records);
		assertThat(events.size()).isEqualTo(1);
		assertThat(events.get(0).getRecordId()).isEqualTo("C30");
		assertThat(events.get(0).getType()).isEqualTo(EventType.BORROW_FOLDER);
		assertThat(events.get(0).getCreatedOn().toLocalDate()).isEqualTo(borrowingDate);
	}

	@Test
	public void givenBorrowedFolderWhenReturnItWithAPastDateThenEventHasAPastDate()
			throws Exception {

		LocalDate borrowingDate = nowDateTime.minusDays(10).toLocalDate();
		LocalDate previewReturnDate = nowDateTime.plusDays(1).toLocalDate();
		LocalDate returnDate = nowDateTime.minusDays(5).toLocalDate();
		borrowingServices
				.borrowFolder(records.getFolder_C30().getId(), borrowingDate, previewReturnDate, records.getChuckNorris(),
						records.getChuckNorris(),
						BorrowingType.BORROW, true);
		borrowingServices
				.returnFolder(records.getFolder_C30().getId(), records.getChuckNorris(), returnDate, true);

		recordServices.flush();
		LogicalSearchQuery query = services
				.newFindReturnedFoldersByDateRangeQuery(records.getChuckNorris(), TimeProvider.getLocalDateTime().minusDays(10),
						TimeProvider.getLocalDateTime());
		List<Record> records = searchServices.search(query);
		List<Event> events = rm.wrapEvents(records);
		assertThat(events.size()).isEqualTo(1);
		assertThat(events.get(0).getRecordId()).isEqualTo("C30");
		assertThat(events.get(0).getType()).isEqualTo(EventType.RETURN_FOLDER);
		assertThat(events.get(0).getCreatedOn().toLocalDate()).isEqualTo(returnDate);
	}

	private RecordWrapper createDocument(String creatorUserName, LocalDateTime eventDate) {
		return createDocument(creatorUserName).setCreatedOn(eventDate);
	}

	private Event createDocument(String creatorUserName) {
		return rm.newEvent().setUsername(creatorUserName).setType(EventType.CREATE_DOCUMENT);
	}

	private RecordWrapper deleteDocument(String creatorUserName, LocalDateTime eventDate) {
		return deleteDocument(creatorUserName).setCreatedOn(eventDate);
	}

	private Event deleteDocument(String creatorUserName) {
		return rm.newEvent().setUsername(creatorUserName).setType(EventType.DELETE_DOCUMENT);
	}

	private RecordWrapper createFolder(String creatorUserName, LocalDateTime eventDate) {
		return createFolder(creatorUserName).setCreatedOn(eventDate);
	}

	private Event createFolder(String creatorUserName) {
		return rm.newEvent().setUsername(creatorUserName).setType(EventType.CREATE_FOLDER);
	}

	private RecordWrapper deleteFolder(String creatorUserName, LocalDateTime eventDate) {
		return deleteFolder(creatorUserName).setCreatedOn(eventDate);
	}

	private Event deleteFolder(String creatorUserName) {
		return rm.newEvent().setUsername(creatorUserName).setType(EventType.DELETE_FOLDER);
	}

	private RecordWrapper grantPermission(Folder folder) {
		return rm.newEvent().setType(EventType.GRANT_PERMISSION).setRecordId(folder.getId());
	}

	private RecordWrapper openSession(String username, LocalDateTime date) {
		return rm.newEvent().setType(EventType.OPEN_SESSION).setUsername(username).setCreatedOn(date);
	}

	private RecordWrapper closeSession(String username, LocalDateTime date) {
		return rm.newEvent().setType(EventType.CLOSE_SESSION).setUsername(username).setCreatedOn(date);
	}

	private Event createFolderInFilingSpace(String principalPath) {
		return rm.newEvent().setType(EventType.CREATE_FOLDER).setEventPrincipalPath(principalPath);
	}

}

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
package com.constellio.app.modules.rm.services.events;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.Event;
import com.constellio.model.entities.records.wrappers.EventType;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.services.logging.LoggingServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;

public class RMEventsSearchServicesAcceptanceTest extends ConstellioTest {

	LocalDateTime testDate = new LocalDateTime().minusMonths(1);

	Users users;

	RMSchemasRecordsServices schemas;

	SearchServices searchServices;

	RMEventsSearchServices rmSchemasRecordsServices;
	private RMTestRecords records;

	@Before
	public void setUp()
			throws Exception {
		givenCollection(zeCollection).withConstellioRMModule().withAllTestUsers().andUsersWithReadAccess("admin");

		schemas = new RMSchemasRecordsServices(zeCollection, getModelLayerFactory());
		searchServices = getModelLayerFactory().newSearchServices();
		rmSchemasRecordsServices = new RMEventsSearchServices(getModelLayerFactory(), zeCollection);
		users = new Users().using(getModelLayerFactory().newUserServices());
		records = new RMTestRecords(zeCollection);
		records.setup(getModelLayerFactory()).withFoldersAndContainersOfEveryStatus();
	}

	//by user
	@Test
	public void whenGetCreatedDocumentsByUserThenReturnValidEvents()
			throws Exception {

		Transaction transaction = new Transaction();
		UserCredential user1 = users.dakotaLIndien();
		RecordWrapper documentCreationByUser1 = createDocument(user1.getUsername()).setRecordId("42").setCreatedOn(testDate);
		String expectedId = transaction
				.add(documentCreationByUser1).getId();
		UserCredential user2 = users.chuckNorris();
		Event documentCreationByUser2 = createDocument(user2.getUsername()).setRecordId("43");
		transaction.add(documentCreationByUser2);

		getModelLayerFactory().newRecordServices().execute(transaction);

		LogicalSearchQuery query = rmSchemasRecordsServices
				.newFindCreatedDocumentsByDateRangeAndByUserQuery(users.adminIn(zeCollection), testDate,
						testDate.plusDays(1), users.dakotaLIndienIn(zeCollection).getUsername());

		List<Event> events = schemas.wrapEvents(searchServices.search(query));
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

		LogicalSearchQuery query = rmSchemasRecordsServices
				.newFindCreatedDocumentsByDateRangeQuery(users.adminIn(zeCollection), testDate, new LocalDateTime());

		List<Event> events = schemas.wrapEvents(searchServices.search(query));
		assertThat(events.size()).isEqualTo(1);
		assertThat(events.get(0).getId().equals(expectedId));
	}

	//by folder
	@Test
	public void whenGetGrantedPermissionsByFolderThenReturnValidEvents()
			throws Exception {
		Folder folder1 = schemas.newFolder();
		Folder folder2 = schemas.newFolder();
		Transaction transaction = new Transaction();
		String expectedId = transaction
				.add(grantPermission(folder1)).getId();
		transaction
				.add(grantPermission(folder2));
		getModelLayerFactory().newRecordServices().execute(transaction);

		LogicalSearchQuery query = rmSchemasRecordsServices
				.newFindGrantedPermissionsByDateRangeAndByFolderQuery(users.adminIn(zeCollection), testDate, new LocalDateTime(),
						folder1);

		List<Event> events = schemas.wrapEvents(searchServices.search(query));
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
		List<Event> borrowedFoldersEvents = rmSchemasRecordsServices
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

		List<Event> loggedUsers = rmSchemasRecordsServices
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

		loggedUsers = rmSchemasRecordsServices
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

		String[] nonAcceptedPaths = { "/c", "/ab", "/b", "/a/b/c/a/b/c" };
		LogicalSearchQuery createdFoldersInFilingSpaceQuery;
		for (String currentNonAcceptedPath : nonAcceptedPaths) {
			createdFoldersInFilingSpaceQuery = rmSchemasRecordsServices
					.newFindCreatedFoldersByDateRangeAndByFilingSpaceQuery(users.adminIn(zeCollection), testDate,
							new LocalDateTime(), currentNonAcceptedPath);
			long count = searchServices.getResultsCount(createdFoldersInFilingSpaceQuery);
			assertThat(count).isEqualTo(0l);
		}

		String[] acceptedPaths = { "/a/b/c/a/b", "/a/b/c/a/b/", "/a/b/c" };
		for (String currentNonAcceptedPath : acceptedPaths) {
			createdFoldersInFilingSpaceQuery = rmSchemasRecordsServices
					.newFindCreatedFoldersByDateRangeAndByFilingSpaceQuery(users.adminIn(zeCollection), testDate,
							new LocalDateTime(), currentNonAcceptedPath);
			List<Event> events = schemas.wrapEvents(searchServices.search(createdFoldersInFilingSpaceQuery));
			assertThat(events.size()).isEqualTo(1);
			assertThat(events.get(0).getEventPrincipalPath()).isEqualTo(filingSpacePath1);
		}
	}

	private RecordWrapper createDocument(String creatorUserName, LocalDateTime eventDate) {
		return createDocument(creatorUserName).setCreatedOn(eventDate);
	}

	private Event createDocument(String creatorUserName) {
		return schemas.newEvent().setUsername(creatorUserName).setType(EventType.CREATE_DOCUMENT);
	}

	private RecordWrapper deleteDocument(String creatorUserName, LocalDateTime eventDate) {
		return deleteDocument(creatorUserName).setCreatedOn(eventDate);
	}

	private Event deleteDocument(String creatorUserName) {
		return schemas.newEvent().setUsername(creatorUserName).setType(EventType.DELETE_DOCUMENT);
	}

	private RecordWrapper createFolder(String creatorUserName, LocalDateTime eventDate) {
		return createFolder(creatorUserName).setCreatedOn(eventDate);
	}

	private Event createFolder(String creatorUserName) {
		return schemas.newEvent().setUsername(creatorUserName).setType(EventType.CREATE_FOLDER);
	}

	private RecordWrapper deleteFolder(String creatorUserName, LocalDateTime eventDate) {
		return deleteFolder(creatorUserName).setCreatedOn(eventDate);
	}

	private Event deleteFolder(String creatorUserName) {
		return schemas.newEvent().setUsername(creatorUserName).setType(EventType.DELETE_FOLDER);
	}

	private RecordWrapper grantPermission(Folder folder) {
		return schemas.newEvent().setType(EventType.GRANT_PERMISSION).setRecordId(folder.getId());
	}

	private RecordWrapper openSession(String username, LocalDateTime date) {
		return schemas.newEvent().setType(EventType.OPEN_SESSION).setUsername(username).setCreatedOn(date);
	}

	private RecordWrapper closeSession(String username, LocalDateTime date) {
		return schemas.newEvent().setType(EventType.CLOSE_SESSION).setUsername(username).setCreatedOn(date);
	}

	private Event createFolderInFilingSpace(String principalPath) {
		return schemas.newEvent().setType(EventType.CREATE_FOLDER).setEventPrincipalPath(principalPath);
	}

}

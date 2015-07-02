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
package com.constellio.app.modules.rm.services.borrowingServices;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;
import java.util.List;

import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.model.enums.FolderStatus;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.borrowingServices.BorrowingServicesRunTimeException.BorrowingServicesRunTimeException_CannotBorrowActiveFolder;
import com.constellio.app.modules.rm.services.borrowingServices.BorrowingServicesRunTimeException.BorrowingServicesRunTimeException_FolderIsAlreadyBorrowed;
import com.constellio.app.modules.rm.services.borrowingServices.BorrowingServicesRunTimeException.BorrowingServicesRunTimeException_FolderIsNotBorrowed;
import com.constellio.app.modules.rm.services.borrowingServices.BorrowingServicesRunTimeException.BorrowingServicesRunTimeException_UserNotAllowedToReturnFolder;
import com.constellio.app.modules.rm.services.events.RMEventsSearchServices;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Event;
import com.constellio.model.entities.records.wrappers.EventType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.search.SearchServices;
import com.constellio.sdk.tests.ConstellioTest;

public class BorrowingServicesAcceptTest extends ConstellioTest {

	RMSchemasRecordsServices rm;
	RMTestRecords records = new RMTestRecords(zeCollection);
	BorrowingServices borrowingServices;
	LocalDateTime nowDateTime = TimeProvider.getLocalDateTime();
	RMEventsSearchServices rmEventsSearchServices;
	SearchServices searchServices;

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers().withRMTest(records)
						.withFoldersAndContainersOfEveryStatus()
		);

		rm = new RMSchemasRecordsServices(zeCollection, getModelLayerFactory());
		borrowingServices = new BorrowingServices(zeCollection, getModelLayerFactory());
		rmEventsSearchServices = new RMEventsSearchServices(getModelLayerFactory(), zeCollection);
		searchServices = getModelLayerFactory().newSearchServices();

		givenTimeIs(nowDateTime);
	}

	@Test
	public void givenSemiActiveFolderWhenBorrowItThenOk()
			throws Exception {

		givenBorrowedFolderC30ByAdmin();

		Folder folderC30 = records.getFolder_C30();
		assertThat(folderC30.getArchivisticStatus()).isEqualTo(FolderStatus.SEMI_ACTIVE);
		assertThat(folderC30.getBorrowed()).isTrue();
		assertThat(folderC30.getBorrowDate()).isEqualTo(nowDateTime);
		assertThat(folderC30.getBorrowReturnDate()).isNull();
		assertThat(folderC30.getBorrowUser()).isEqualTo(records.getAdmin().getId());
		assertThat(folderC30.getBorrowUserEntered()).isEqualTo(records.getAdmin().getId());
		assertThat(
				searchServices.getResultsCount(rmEventsSearchServices.newFindCurrentlyBorrowedFoldersQuery(records.getAdmin())))
				.isEqualTo(1);
	}

	@Test
	public void givenInactiveFolderWhenBorrowItThenOk()
			throws Exception {

		Date previewReturnDate = nowDateTime.plusDays(15).toDate();
		Folder folderA94 = records.getFolder_A94();

		borrowingServices.borrowFolder(folderA94.getId(), previewReturnDate, records.getAdmin(), records.getAlice());
		folderA94 = records.getFolder_A94();

		assertThat(folderA94.getArchivisticStatus()).isEqualTo(FolderStatus.INACTIVATE_DEPOSITED);
		assertThat(folderA94.getBorrowed()).isTrue();
		assertThat(folderA94.getBorrowDate()).isEqualTo(nowDateTime);
		assertThat(folderA94.getBorrowReturnDate()).isNull();
		assertThat(folderA94.getBorrowUser()).isEqualTo(records.getAdmin().getId());
		assertThat(folderA94.getBorrowUserEntered()).isEqualTo(records.getAlice().getId());
		assertThat(
				searchServices.getResultsCount(rmEventsSearchServices.newFindCurrentlyBorrowedFoldersQuery(records.getAdmin())))
				.isEqualTo(1);
	}

	@Test(expected = BorrowingServicesRunTimeException_CannotBorrowActiveFolder.class)
	public void givenActiveFolderWhenBorrowItThenCannotBorrow()
			throws Exception {

		Date previewReturnDate = nowDateTime.plusDays(15).toDate();
		Folder folderA16 = records.getFolder_A16();

		try {
			borrowingServices.borrowFolder(folderA16.getId(), previewReturnDate, records.getAdmin(), records.getAdmin());
		} finally {
			folderA16 = records.getFolder_A16();
			assertThat(folderA16.getArchivisticStatus()).isEqualTo(FolderStatus.ACTIVE);
			assertThat(folderA16.getBorrowed()).isNull();
			assertThat(folderA16.getBorrowDate()).isNull();
			assertThat(folderA16.getBorrowReturnDate()).isNull();
			assertThat(folderA16.getBorrowUser()).isNull();
			assertThat(folderA16.getBorrowUserEntered()).isNull();
			assertThat(searchServices
					.getResultsCount(rmEventsSearchServices.newFindCurrentlyBorrowedFoldersQuery(records.getAdmin())))
					.isEqualTo(0);
		}
	}

	@Test(expected = BorrowingServicesRunTimeException_FolderIsAlreadyBorrowed.class)
	public void givenBorrowedFolderWhenBorrowItThenCannotBorrow()
			throws Exception {

		Date previewReturnDate = nowDateTime.plusDays(15).toDate();
		Folder folderC30 = records.getFolder_C30();
		borrowingServices.borrowFolder(folderC30.getId(), previewReturnDate, records.getAdmin(), records.getAdmin());

		try {
			borrowingServices.borrowFolder(folderC30.getId(), previewReturnDate, records.getAdmin(), records.getAdmin());
		} finally {
			folderC30 = records.getFolder_C30();
			assertThat(folderC30.getArchivisticStatus()).isEqualTo(FolderStatus.SEMI_ACTIVE);
			assertThat(folderC30.getBorrowed()).isTrue();
			assertThat(folderC30.getBorrowDate()).isEqualTo(nowDateTime);
			assertThat(folderC30.getBorrowReturnDate()).isNull();
			assertThat(folderC30.getBorrowUser()).isEqualTo(records.getAdmin().getId());
			assertThat(folderC30.getBorrowUserEntered()).isEqualTo(records.getAdmin().getId());
			assertThat(searchServices
					.getResultsCount(rmEventsSearchServices.newFindCurrentlyBorrowedFoldersQuery(records.getAdmin())))
					.isEqualTo(1);
		}
	}

	//Return folder

	@Test
	public void whenReturnFolderThenOk()
			throws Exception {

		givenBorrowedFolderC30ByAdmin();
		Folder folderC30 = records.getFolder_C30();

		givenTimeIs(nowDateTime.plusDays(1));
		borrowingServices.returnFolder(folderC30.getId(), records.getAdmin());
		folderC30 = records.getFolder_C30();

		assertThat(folderC30.getArchivisticStatus()).isEqualTo(FolderStatus.SEMI_ACTIVE);
		assertThat(folderC30.getBorrowed()).isNull();
		assertThat(folderC30.getBorrowDate()).isNull();
		assertThat(folderC30.getBorrowReturnDate()).isNull();
		assertThat(folderC30.getBorrowUser()).isNull();
		assertThat(folderC30.getBorrowUserEntered()).isNull();
		assertThat(
				searchServices.getResultsCount(rmEventsSearchServices.newFindCurrentlyBorrowedFoldersQuery(records.getAdmin())))
				.isEqualTo(0);
		Thread.sleep(1000);
		List<Record> records = searchServices.search(rmEventsSearchServices.newFindReturnedFoldersByDateRangeQuery(
				this.records.getAdmin(),
				TimeProvider.getLocalDateTime().minusDays(1), TimeProvider.getLocalDateTime().plusDays(1)));
		assertThat(records).hasSize(1);
		Event event = new Event(records.get(0), getSchemaTypes());
		assertThat(event.getUsername()).isEqualTo(this.records.getAdmin().getUsername());
		assertThat(event.getType()).isEqualTo(EventType.RETURN_FOLDER);
		assertThat(event.getCreatedOn()).isEqualTo(nowDateTime.plusDays(1));
	}

	@Test
	public void givenAdminUserBorrowFolderToUserWhenHeReturnsFolderThenOk()
			throws Exception {

		Date previewReturnDate = nowDateTime.plusDays(15).toDate();
		borrowingServices
				.borrowFolder(records.getFolder_C30().getId(), previewReturnDate, records.getAdmin(), records.getBob_userInAC());
		Folder folderC30 = records.getFolder_C30();

		givenTimeIs(nowDateTime.plusDays(1));
		borrowingServices.returnFolder(folderC30.getId(), records.getBob_userInAC());
		folderC30 = records.getFolder_C30();

		assertThat(folderC30.getArchivisticStatus()).isEqualTo(FolderStatus.SEMI_ACTIVE);
		assertThat(folderC30.getBorrowed()).isNull();
		assertThat(folderC30.getBorrowDate()).isNull();
		assertThat(folderC30.getBorrowReturnDate()).isNull();
		assertThat(folderC30.getBorrowUser()).isNull();
		assertThat(folderC30.getBorrowUserEntered()).isNull();
		assertThat(
				searchServices.getResultsCount(rmEventsSearchServices.newFindCurrentlyBorrowedFoldersQuery(records.getAdmin())))
				.isEqualTo(0);
		Thread.sleep(1000);
		List<Record> records = searchServices.search(rmEventsSearchServices.newFindReturnedFoldersByDateRangeQuery(
				this.records.getAdmin(),
				TimeProvider.getLocalDateTime().minusDays(1), TimeProvider.getLocalDateTime().plusDays(1)));
		assertThat(records).hasSize(1);
		Event event = new Event(records.get(0), getSchemaTypes());
		assertThat(event.getUsername()).isEqualTo(this.records.getBob_userInAC().getUsername());
		assertThat(event.getType()).isEqualTo(EventType.RETURN_FOLDER);
		assertThat(event.getCreatedOn()).isEqualTo(nowDateTime.plusDays(1));
	}

	@Test
	public void givenAdminUserBorrowFolderToBobWhenAdminReturnsFolderThenOk()
			throws Exception {

		Date previewReturnDate = nowDateTime.plusDays(15).toDate();
		borrowingServices
				.borrowFolder(records.getFolder_C30().getId(), previewReturnDate, records.getAdmin(), records.getBob_userInAC());
		Folder folderC30 = records.getFolder_C30();

		givenTimeIs(nowDateTime.plusDays(1));
		borrowingServices.returnFolder(folderC30.getId(), records.getAdmin());
		folderC30 = records.getFolder_C30();

		assertThat(folderC30.getArchivisticStatus()).isEqualTo(FolderStatus.SEMI_ACTIVE);
		assertThat(folderC30.getBorrowed()).isNull();
		assertThat(folderC30.getBorrowDate()).isNull();
		assertThat(folderC30.getBorrowReturnDate()).isNull();
		assertThat(folderC30.getBorrowUser()).isNull();
		assertThat(folderC30.getBorrowUserEntered()).isNull();
		assertThat(
				searchServices.getResultsCount(rmEventsSearchServices.newFindCurrentlyBorrowedFoldersQuery(records.getAdmin())))
				.isEqualTo(0);

		Thread.sleep(1000);
		List<Record> records = searchServices.search(rmEventsSearchServices.newFindReturnedFoldersByDateRangeQuery(
				this.records.getAdmin(),
				TimeProvider.getLocalDateTime().minusDays(1), TimeProvider.getLocalDateTime().plusDays(1)));
		assertThat(records).hasSize(1);
		Event event = new Event(records.get(0), getSchemaTypes());
		assertThat(event.getUsername()).isEqualTo(this.records.getAdmin().getUsername());
		assertThat(event.getType()).isEqualTo(EventType.RETURN_FOLDER);
		assertThat(event.getCreatedOn()).isEqualTo(nowDateTime.plusDays(1));
	}

	@Test(expected = BorrowingServicesRunTimeException_UserNotAllowedToReturnFolder.class)
	public void whenReturnFolderWithDifferentUserWithoutRGDRoleThenException()
			throws Exception {

		givenBorrowedFolderC30ByAdmin();
		Folder folderC30 = records.getFolder_C30();

		givenTimeIs(nowDateTime.plusDays(1));
		borrowingServices.returnFolder(folderC30.getId(), records.getBob_userInAC());
		assertThat(
				searchServices.getResultsCount(rmEventsSearchServices.newFindCurrentlyBorrowedFoldersQuery(records.getAdmin())))
				.isEqualTo(0);
		Thread.sleep(1000);
		List<Record> records = searchServices.search(rmEventsSearchServices.newFindReturnedFoldersByDateRangeQuery(
				this.records.getAdmin(),
				TimeProvider.getLocalDateTime().minusDays(1), TimeProvider.getLocalDateTime().plusDays(1)));
		assertThat(records).isEmpty();
	}

	@Test(expected = BorrowingServicesRunTimeException_FolderIsNotBorrowed.class)
	public void givenNotBorrowedFolderWhenReturnItThenException()
			throws Exception {

		Folder folderC30 = records.getFolder_C30();

		borrowingServices.returnFolder(folderC30.getId(), records.getAdmin());

		assertThat(
				searchServices.getResultsCount(rmEventsSearchServices.newFindCurrentlyBorrowedFoldersQuery(records.getAdmin())))
				.isEqualTo(0);
		Thread.sleep(1000);
		List<Record> records = searchServices.search(rmEventsSearchServices.newFindReturnedFoldersByDateRangeQuery(
				this.records.getAdmin(),
				TimeProvider.getLocalDateTime().minusDays(1), TimeProvider.getLocalDateTime().plusDays(1)));
		assertThat(records).isEmpty();
	}

	private void givenBorrowedFolderC30ByAdmin()
			throws RecordServicesException {
		Date previewReturnDate = nowDateTime.plusDays(15).toDate();
		borrowingServices
				.borrowFolder(records.getFolder_C30().getId(), previewReturnDate, records.getAdmin(), records.getAdmin());
	}

	private MetadataSchemaTypes getSchemaTypes() {
		return getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection);
	}
}

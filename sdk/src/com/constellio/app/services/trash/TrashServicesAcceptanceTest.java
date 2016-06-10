package com.constellio.app.services.trash;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;

public class TrashServicesAcceptanceTest extends ConstellioTest {
	TrashServices trashServices;
	Users users = new Users();
	RMTestRecords records = new RMTestRecords(zeCollection);

	private RecordServices recordServices;
	private String folderDeletedLogicallyId, documentDeletedLogicallyId, categoryDeletedLogicallyId;
	User admin;
	private SearchServices searchServices;

	@Before
	public void setUp()
			throws Exception {
		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers().withRMTest(records)
						.withFoldersAndContainersOfEveryStatus()
		);

		recordServices = getModelLayerFactory().newRecordServices();
		users.setUp(getModelLayerFactory().newUserServices());
		trashServices = new TrashServices(getAppLayerFactory(), zeCollection);
		admin = users.adminIn(zeCollection);
		searchServices = getModelLayerFactory().newSearchServices();

		initTests();
	}

	private void initTests()
			throws RecordServicesException {
		deleteLogicallyAFolderADocumentAndACategory();
	}

	private void deleteLogicallyAFolderADocumentAndACategory()
			throws RecordServicesException {
		LocalDateTime now;
		givenTimeIs(now = TimeProvider.getLocalDateTime());
		User adminUser = users.adminIn(zeCollection);
		Folder folderA1 = records.getFolder_A01();
		folderDeletedLogicallyId = folderA1.getId();
		recordServices.logicallyDelete(folderA1.getWrappedRecord(), adminUser);
		Category category = records.getCategory_X13();
		categoryDeletedLogicallyId = category.getId();
		recordServices.logicallyDelete(category.getWrappedRecord(), adminUser);
		givenTimeIs(now.plusDays(1));
		Document document = records.getDocumentWithContent_A49();
		documentDeletedLogicallyId = document.getId();
		recordServices.logicallyDelete(document.getWrappedRecord(), adminUser);
		recordServices.add(folderA1.set(Schemas.ERROR_ON_PHYSICAL_DELETION.getLocalCode(), true));
	}

	@Test
	public void whenGetTrashRecordsQueryForTypeThenOk()
			throws Exception {
		whenGetTrashRecordsQueryForFoldersTypeThenOk();
		whenGetTrashRecordsQueryForDocumentsTypeThenOk();
		whenGetTrashRecordsQueryForCategoryTypeThenOk();
	}

	private void whenGetTrashRecordsQueryForCategoryTypeThenOk() {
		LogicalSearchQuery query = trashServices
				.getTrashRecordsQueryForType(Folder.SCHEMA_TYPE, admin);
		List<String> recordIds = getQueryResultsIds(query);
		assertThat(recordIds).contains(folderDeletedLogicallyId);
		assertThat(recordIds).doesNotContain(documentDeletedLogicallyId, categoryDeletedLogicallyId);
	}

	private List<String> getQueryResultsIds(LogicalSearchQuery query) {
		List<Record> result = searchServices.search(query);
		List<String> returnList = new ArrayList<>();
		for(Record record : result){
			returnList.add(record.getId());
		}
		return  returnList;
	}

	private void whenGetTrashRecordsQueryForDocumentsTypeThenOk() {
		LogicalSearchQuery query = trashServices
				.getTrashRecordsQueryForType(Document.SCHEMA_TYPE, admin);
		List<String> recordIds = getQueryResultsIds(query);
		assertThat(recordIds).contains(documentDeletedLogicallyId);
		assertThat(recordIds).doesNotContain(folderDeletedLogicallyId, categoryDeletedLogicallyId);
	}

	private void whenGetTrashRecordsQueryForFoldersTypeThenOk() {
		LogicalSearchQuery query = trashServices
				.getTrashRecordsQueryForType(Category.SCHEMA_TYPE, admin);
		List<String> recordIds = getQueryResultsIds(query);
		assertThat(recordIds).contains(categoryDeletedLogicallyId);
		assertThat(recordIds).doesNotContain(documentDeletedLogicallyId, folderDeletedLogicallyId);
	}

	//getTrashRecordsQueryForType
	//getTrashRecordsQueryForCollection
	//deleteAll
	//restoreSelection
	//deleteSelection
	//getTypesWithLogicallyDeletedRecords
	//getTypesWithLogicallyDeletedRecords
}

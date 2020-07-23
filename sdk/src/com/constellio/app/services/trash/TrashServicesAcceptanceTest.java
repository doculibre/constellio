package com.constellio.app.services.trash;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Cart;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.services.TasksSchemasRecordsServices;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.RecordServicesRuntimeException.NoSuchRecordWithId;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.security.AuthorizationsServices;
import com.constellio.model.services.trash.TrashServices;
import com.constellio.model.services.users.SystemWideUserInfos;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.constellio.model.entities.security.global.AuthorizationAddRequest.authorizationForUsers;
import static com.constellio.sdk.tests.TestUtils.assertThatRecord;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.junit.Assert.fail;

public class TrashServicesAcceptanceTest extends ConstellioTest {
	TrashServices trashServices;
	Users users = new Users();
	RMTestRecords records = new RMTestRecords(zeCollection);

	private RecordServices recordServices;
	private String folderDeletedLogicallyId, documentDeletedLogicallyId, categoryDeletedLogicallyId,
			documentInADeletedFolder, documentInADeletedFolderTitle;
	User admin;
	private SearchServices searchServices;
	private Task task;
	private RMSchemasRecordsServices rm;

	@Before
	public void setUp()
			throws Exception {
		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers().withRMTest(records)
						.withFoldersAndContainersOfEveryStatus().withDocumentsHavingContent(),
				withCollection(businessCollection).withTasksModule().withAllTestUsers()
		);

		recordServices = getModelLayerFactory().newRecordServices();
		users.setUp(getModelLayerFactory().newUserServices());
		trashServices = new TrashServices(getAppLayerFactory().getModelLayerFactory(), zeCollection);
		admin = users.adminIn(zeCollection);
		searchServices = getModelLayerFactory().newSearchServices();
		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());

		initTests();
	}

	private void initTests()
			throws RecordServicesException {
		deleteLogicallyAFolderADocumentAndACategory();
		deleteTaskFromBusinessCollection();
		deleteUserChuck();
		deleteCartLogically();
	}

	private void deleteCartLogically()
			throws RecordServicesException {
		Cart cart = rm.newCart();
		User adminInZCollection = users.adminIn(zeCollection);
		recordServices.add(cart.setOwner(adminInZCollection).setTitle("zCart"));
		recordServices.logicallyDelete(cart.getWrappedRecord(), adminInZCollection);
	}

	private void deleteUserChuck() {
		UserServices userServices = getModelLayerFactory()
				.newUserServices();
		SystemWideUserInfos chuck = users.chuckNorris();
		userServices.removeUserCredentialAndUser(chuck);
	}

	private void deleteTaskFromBusinessCollection()
			throws RecordServicesException {
		TasksSchemasRecordsServices taskSchemas = new TasksSchemasRecordsServices(businessCollection, getAppLayerFactory());
		task = taskSchemas.newTask().setTitle("title");
		recordServices.add(task);
		User adminInBusiness = users.adminIn(businessCollection);

		AuthorizationsServices authorizationService = getModelLayerFactory().newAuthorizationsServices();
		authorizationService.add(authorizationForUsers(adminInBusiness).on(task).giving("d"));
		recordServices.logicallyDelete(task.getWrappedRecord(), null);
	}

	private void deleteLogicallyAFolderADocumentAndACategory()
			throws RecordServicesException {
		LocalDateTime now;
		givenTimeIs(now = TimeProvider.getLocalDateTime());
		User adminUser = users.adminIn(zeCollection);
		Folder folderA1 = records.getFolder_A01();
		folderDeletedLogicallyId = folderA1.getId();
		Document doc = rm.newDocument().setFolder(folderA1).setTitle("zDoc");
		recordServices.add(doc);
		documentInADeletedFolder = doc.getId();
		documentInADeletedFolderTitle = doc.getTitle();
		recordServices.logicallyDelete(folderA1.getWrappedRecord(), adminUser);
		Category category = records.getCategory_X13();
		categoryDeletedLogicallyId = category.getId();
		recordServices.logicallyDelete(category.getWrappedRecord(), adminUser);
		givenTimeIs(now.plusDays(1));
		Document document = records.getDocumentWithContent_A49();
		documentDeletedLogicallyId = document.getId();
		recordServices.logicallyDelete(document.getWrappedRecord(), adminUser);
		recordServices.add((RecordWrapper) folderA1.set(Schemas.ERROR_ON_PHYSICAL_DELETION.getLocalCode(), true));
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
		for (Record record : result) {
			returnList.add(record.getId());
		}
		return returnList;
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

	@Test
	public void whenGetTrashRecordsQueryForCollectionThenOk()
			throws Exception {
		LogicalSearchQuery query = trashServices
				.getTrashRecordsQueryForCollection(zeCollection, admin);
		List<String> recordIds = getQueryResultsIds(query);
		assertThat(recordIds).contains(categoryDeletedLogicallyId, documentDeletedLogicallyId, folderDeletedLogicallyId);
		assertThat(recordIds).doesNotContain(task.getId());
	}

	@Test
	public void whenGetTypesWithLogicallyDeletedRecordsThenOk()
			throws Exception {
		Set<String> types = trashServices.getTypesWithLogicallyDeletedRecords(zeCollection, admin);
		assertThat(types).containsOnly(Folder.SCHEMA_TYPE, Document.SCHEMA_TYPE, Category.SCHEMA_TYPE);
	}

	@Test
	public void whenGetLogicallyDeletedRecordsCountThenOk()
			throws Exception {
		long count = trashServices.getLogicallyDeletedRecordsCount(zeCollection, admin);
		assertThat(count).isEqualTo(8);
	}

	@Test
	public void givenRestorableRecordWhenRestoreSelectionThenOk()
			throws Exception {
		Map<String, String> notRestored = trashServices.restoreSelection(new HashSet<>(asList(folderDeletedLogicallyId)), admin);
		assertThat(notRestored).isEmpty();
		Record folder = recordServices.getDocumentById(folderDeletedLogicallyId);
		assertThatRecord(folder).hasMetadataValue(Schemas.LOGICALLY_DELETED_STATUS, false);
		assertThatRecord(folder).hasMetadataValue(Schemas.LOGICALLY_DELETED_ON, null);
	}

	@Test
	public void givenNonRestorableRecordWhenRestoreSelectionThenRecordNotRestored()
			throws Exception {
		Map<String, String> notRestored = trashServices.restoreSelection(new HashSet<>(asList(documentInADeletedFolder)), admin);
		assertThat(notRestored).containsOnly(entry(documentInADeletedFolder, documentInADeletedFolderTitle));
		;
		Record doc = recordServices.getDocumentById(documentInADeletedFolder);
		assertThatRecord(doc).hasMetadataValue(Schemas.LOGICALLY_DELETED_STATUS, true);
	}

	@Test
	public void givenDeletableRecordWhenDeleteSelectionThenOk()
			throws Exception {
		trashServices.deleteSelection(new HashSet<>(asList(documentInADeletedFolder)), admin);
		try {
			recordServices.getDocumentById(documentInADeletedFolder);
			fail("was not deleted physically");
		} catch (NoSuchRecordWithId e) {
			//OK
		}
	}

	@Test
	public void givenNonDeletableRecordWhenDeleteSelectionThenNotDeleted()
			throws Exception {
		//TODO and add another test with problematic delete
		trashServices.deleteSelection(new HashSet<>(asList(folderDeletedLogicallyId)), admin);
		try {
			recordServices.getDocumentById(folderDeletedLogicallyId);
			fail("was not deleted physically");
		} catch (NoSuchRecordWithId e) {
			//OK
		}
	}

}

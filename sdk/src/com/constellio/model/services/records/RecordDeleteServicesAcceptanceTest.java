package com.constellio.model.services.records;

import org.junit.Before;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.trash.TrashServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;

public class RecordDeleteServicesAcceptanceTest extends ConstellioTest {

	TrashServices trashServices;
	Users users = new Users();
	RMTestRecords records = new RMTestRecords(zeCollection);

	private RecordServices recordServices;
	private String folderDeletedLogicallyId, documentDeletedLogicallyId, categoryDeletedLogicallyId,
			documentInADeletedFolder;
	User admin;
	private SearchServices searchServices;
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

	}
}

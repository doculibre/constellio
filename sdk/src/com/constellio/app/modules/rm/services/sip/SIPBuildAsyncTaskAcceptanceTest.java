package com.constellio.app.modules.rm.services.sip;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.SIParchive;
import com.constellio.app.ui.framework.buttons.SIPButton.SIPBuildAsyncTask;
import com.constellio.model.entities.batchprocess.AsyncTaskCreationRequest;
import com.constellio.model.entities.records.wrappers.TemporaryRecord;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.services.records.RecordServicesRuntimeException;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.ALL;
import static com.constellio.sdk.tests.TestUtils.assertThatRecords;
import static java.util.Arrays.asList;
import static org.junit.Assert.fail;

public class SIPBuildAsyncTaskAcceptanceTest extends ConstellioTest {

	RMTestRecords records = new RMTestRecords(zeCollection);
	SearchServices searchServices;
	RMSchemasRecordsServices rm;

	@Before
	public void setup() {
		prepareSystem(
				withZeCollection().withConstellioRMModule().withRMTest(records)
						.withFoldersAndContainersOfEveryStatus().withAllTestUsers()
		);
		this.searchServices = getModelLayerFactory().newSearchServices();
		this.rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
	}

	@Test
	public void testIfEmptyFileNameThrowError() {
		try {
			new SIPBuildAsyncTask("", asList("test1", "test2"), Collections.<String>emptyList(),
					Collections.singletonList(records.getFolder_A01().getId()), false, records.getAdmin().getUsername(), false,
					getAppLayerFactory().newApplicationService().getWarVersion(), "fr");
			fail();
		} catch (Exception e) {
			//OK!
		}
	}

	@Test
	public void testIfEmptyUsernameThrowError() {
		try {
			new SIPBuildAsyncTask("testFileName", Collections.<String>emptyList(), Collections.<String>emptyList(),
					Collections.singletonList(records.getFolder_A01().getId()), false, "", false,
					getAppLayerFactory().newApplicationService().getWarVersion(), "fr");
			fail();
		} catch (Exception e) {
			//Ok !
		}
	}

	@Test
	public void checkIfWarVersionIsNullThrowException() {
		try {
			new SIPBuildAsyncTask("testFileName", Collections.<String>emptyList(), Collections.<String>emptyList(),
					Collections.singletonList(records.getFolder_A01().getId()), false, records.getAdmin().getUsername(), false,
					"", "fr");
			fail();
		} catch (Exception e) {
			// Ok !
		}
	}

	@Test
	public void checkIfSIPArchiveIsCorrectlyCreated()
			throws Exception {
		String testfileName = "testFileName";
		SIPBuildAsyncTask task = new SIPBuildAsyncTask(testfileName, Collections.<String>emptyList(),
				Collections.<String>emptyList(), Collections.singletonList(records.getFolder_A01().getId()), false,
				records.getAdmin().getUsername(), false, getAppLayerFactory().newApplicationService().getWarVersion(), "fr");
		getAppLayerFactory().getModelLayerFactory().getBatchProcessesManager().addAsyncTask(
				new AsyncTaskCreationRequest(task, zeCollection,
						"SIPArchive from test com.constellio.app.modules.rm.services.sip.SIPBuildAsyncTaskAcceptanceTest"));
		waitForBatchProcess();

		MetadataSchema sipArchiveSchema = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection)
				.getSchemaType(SIParchive.SCHEMA_TYPE).getCustomSchema(SIParchive.SCHEMA_NAME);
		LogicalSearchCondition allCondition = LogicalSearchQueryOperators.from(sipArchiveSchema).where(ALL);
		List<TemporaryRecord> records = rm.wrapTemporaryRecords(searchServices.search(new LogicalSearchQuery(allCondition)));
		assertThatRecords(records).extractingMetadata("title").containsOnly(
				"testFileName (1 de 5).zip",
				"testFileName (2 de 5).zip",
				"testFileName (3 de 5).zip",
				"testFileName (4 de 5).zip",
				"testFileName (5 de 5).zip"
		);
	}

	@Test
	public void checkIfFilesAreDeletedAfterSIPCreation() {
		try {
			String testfileName = "testFileName";
			SIPBuildAsyncTask task = new SIPBuildAsyncTask(testfileName, Collections.<String>emptyList(),
					Collections.<String>emptyList(), Collections.singletonList(records.getFolder_A01().getId()), false,
					records.getAdmin().getUsername(), true, getAppLayerFactory().newApplicationService().getWarVersion(), "fr");
			getAppLayerFactory().getModelLayerFactory().getBatchProcessesManager().addAsyncTask(
					new AsyncTaskCreationRequest(task, zeCollection,
							"SIPArchive from test com.constellio.app.modules.rm.services.sip.SIPBuildAsyncTaskAcceptanceTest"));
			waitForBatchProcess();

			Folder folderA01 = rm.getFolder(records.folder_A01);
			fail();
		} catch (InterruptedException e) {
			fail();
		} catch (RecordServicesRuntimeException.NoSuchRecordWithId e) {
			//OK!
		}
	}
}

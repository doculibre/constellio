package com.constellio.app.modules.rm.services.decommissioning;

import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.model.enums.FolderStatus;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.decommissioning.DecommissioningServiceException.DecommissioningServiceException_CannotDecommission;
import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.structures.DecomListContainerDetail;
import com.constellio.model.entities.batchprocess.AsyncTaskBatchProcess;
import com.constellio.model.entities.batchprocess.AsyncTaskCreationRequest;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.batch.manager.BatchProcessesManager;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.model.entities.enums.MemoryConsumptionLevel.BETTER_PERFORMANCE;
import static com.constellio.model.entities.enums.MemoryConsumptionLevel.LESS_MEMORY_CONSUMPTION;
import static com.constellio.model.entities.enums.MemoryConsumptionLevel.NORMAL;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromAllSchemasIn;
import static com.constellio.sdk.tests.TestUtils.assertThatRecord;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

public class DecommissionerAcceptanceTest extends ConstellioTest {

	RMTestRecords records = new RMTestRecords(zeCollection);
	RMSchemasRecordsServices rm;
	Users users = new Users();

	@Before
	public void setUp()
			throws Exception {

		givenBackgroundThreadsEnabled();
		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers().withRMTest(records)
						.withFoldersAndContainersOfEveryStatus().withEvents()
		);

		inCollection(zeCollection).setCollectionTitleTo("Collection de test");
		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		users.setUp(getModelLayerFactory().newUserServices());
	}

	@Test
	public void givenTwoDecommissionWithoutCacheReload() throws Exception {
		givenConfig(RMConfigs.REQUIRE_APPROVAL_FOR_CLOSING, false);
		givenConfig(RMConfigs.REQUIRE_APPROVAL_FOR_TRANSFER, false);
		givenConfig(RMConfigs.REQUIRE_APPROVAL_FOR_DEPOSIT_OF_ACTIVE, false);
		givenConfig(RMConfigs.REQUIRE_APPROVAL_FOR_DEPOSIT_OF_SEMIACTIVE, false);
		givenConfig(RMConfigs.REQUIRE_APPROVAL_FOR_DESTRUCTION_OF_ACTIVE, false);
		givenConfig(RMConfigs.REQUIRE_APPROVAL_FOR_DESTRUCTION_OF_SEMIACTIVE, false);

		DecommissioningList decommissioningList = records.getList17();

		DecommissioningAsyncTask asyncTask = new DecommissioningAsyncTask(zeCollection, users.adminIn(zeCollection).getUsername(), decommissioningList.getId());
		AsyncTaskCreationRequest request = new AsyncTaskCreationRequest(asyncTask, zeCollection, "decommissionBatchProcess_17");
		request.setUsername(users.adminIn(zeCollection).getUsername());

		final BatchProcessesManager batchProcessesManager = getModelLayerFactory().getBatchProcessesManager();
		RecordServices recordServices = getModelLayerFactory().newRecordServices();
		AsyncTaskBatchProcess batchProcess = batchProcessesManager.addAsyncTask(request);
		decommissioningList.setCurrentBatchProcessId(batchProcess.getId());
		try {
			recordServices.update(decommissioningList);
		} catch (RecordServicesException e) {
			fail(new DecommissioningServiceException_CannotDecommission().getMessage());
		}

		waitForBatchProcess();

		assertThat(records.getList17().isProcessed()).isTrue();


		decommissioningList = records.getList02();

		asyncTask = new DecommissioningAsyncTask(zeCollection, users.adminIn(zeCollection).getUsername(), decommissioningList.getId());
		request = new AsyncTaskCreationRequest(asyncTask, zeCollection, "decommissionBatchProcess_02");
		request.setUsername(users.adminIn(zeCollection).getUsername());

		batchProcess = batchProcessesManager.addAsyncTask(request);
		decommissioningList.setCurrentBatchProcessId(batchProcess.getId());
		try {
			recordServices.update(decommissioningList);
		} catch (RecordServicesException e) {
			fail(new DecommissioningServiceException_CannotDecommission().getMessage());
		}

		waitForBatchProcess();

		assertThat(records.getList02().isProcessed()).isTrue();
	}

	@Test
	public void givenDecommissioningListWithTypeFoldersToCloseThenClosingDateIsSet() throws Exception {
		DecommissioningList list24 = records.getList24();
		ClosingDecommissioner closingDecommissioner = new ClosingDecommissioner(new DecommissioningService(zeCollection, getAppLayerFactory()), getAppLayerFactory());
		LocalDate now = LocalDate.now();

		assertThat(records.getFolder_A01().getCloseDateEntered()).isNotEqualTo(now);

		closingDecommissioner.process(list24, records.getAdmin(), now);
		waitForBatchProcess();

		assertThat(records.getFolder_A01().getCloseDateEntered()).isEqualTo(now);
	}

	@Test
	public void givenDecommissioningListWithoutTypeFoldersToCloseThenClosingDateIsNotSet() throws Exception {
		givenConfig(RMConfigs.REQUIRE_APPROVAL_FOR_TRANSFER, false);
		getModelLayerFactory().newRecordServices().update(records.getFolder_A22().setCloseDateEntered(null));
		DecommissioningList list16 = records.getList16();
		TransferringDecommissioner transferringDecommissioner = new TransferringDecommissioner(new DecommissioningService(zeCollection, getAppLayerFactory()), getAppLayerFactory());
		LocalDate now = LocalDate.now();

		assertThat(records.getFolder_A22().getCloseDateEntered()).isNotEqualTo(now);

		transferringDecommissioner.process(list16, records.getAdmin(), now);
		waitForBatchProcess();

		assertThat(records.getFolder_A22().getCloseDateEntered()).isNotEqualTo(now);
	}

	@Test
	public void givenFolderDecommissioningListProcessedThenDocumentArchivisticStatusIsUpdated() throws Exception {
		givenConfig(RMConfigs.REQUIRE_APPROVAL_FOR_DESTRUCTION_OF_SEMIACTIVE, false);
		DecommissioningList list02 = records.getList02();
		DestroyingDecommissioner destroyingDecommissioner = new DestroyingDecommissioner(new DecommissioningService(zeCollection, getAppLayerFactory()), getAppLayerFactory());
		LocalDate now = LocalDate.now();

		List<Record> processedDocuments = getModelLayerFactory().newSearchServices().search(new LogicalSearchQuery()
				.setCondition(from(rm.document.schemaType()).where(rm.document.folder()).isEqualTo(records.folder_A54)));
		assertThatRecord(processedDocuments.get(0)).extracting(Document.FOLDER_ARCHIVISTIC_STATUS).isEqualTo(asList(FolderStatus.SEMI_ACTIVE));

		destroyingDecommissioner.process(list02, records.getAdmin(), now);
		waitForBatchProcess();

		processedDocuments = getModelLayerFactory().newSearchServices().search(new LogicalSearchQuery()
				.setCondition(from(rm.document.schemaType()).where(rm.document.folder()).isEqualTo(records.folder_A54)));
		assertThatRecord(processedDocuments.get(0)).extracting(Document.FOLDER_ARCHIVISTIC_STATUS).isEqualTo(asList(FolderStatus.INACTIVE_DESTROYED));
	}

	@Test
	public void givenDestructionDecommisisonerAndConfigToDeleteFolderThenDeletePhysicallyFolders() throws Exception {
		givenConfig(RMConfigs.REQUIRE_APPROVAL_FOR_DESTRUCTION_OF_SEMIACTIVE, false);
		givenConfig(RMConfigs.DELETE_FOLDER_RECORDS_WITH_DESTRUCTION, true);
		DecommissioningList list02 = records.getList02();
		DestroyingDecommissioner destroyingDecommissioner = new DestroyingDecommissioner(new DecommissioningService(zeCollection, getAppLayerFactory()), getAppLayerFactory());
		LocalDate now = LocalDate.now();

		List<String> processedFolders = getModelLayerFactory().newSearchServices().searchRecordIds(new LogicalSearchQuery()
				.setCondition(from(rm.folder.schemaType()).where(Schemas.IDENTIFIER).isIn(list02.getFolders())));
		assertThat(processedFolders).hasSize(3);

		destroyingDecommissioner.process(list02, records.getAdmin(), now);
		processedFolders = getModelLayerFactory().newSearchServices().searchRecordIds(new LogicalSearchQuery(fromAllSchemasIn(zeCollection)
				.where(Schemas.IDENTIFIER).isIn(processedFolders)));
		waitForBatchProcess();

		assertThat(getModelLayerFactory().newRecordServices().getRecordsById(zeCollection, processedFolders)).hasSize(0);
	}

	@Test
	public void givenDecommissionerWithMultipleContainerDetailsForTheSameContainerThenKeepOneThatIsFull()
			throws Exception {
		givenConfig(RMConfigs.REQUIRE_APPROVAL_FOR_DEPOSIT_OF_SEMIACTIVE, false);
		DecommissioningList list07 = records.getList17();
		List<DecomListContainerDetail> details = new ArrayList<>(list07.getContainerDetails());
		details.add(new DecomListContainerDetail(records.getContainerBac11()).setFull(true));
		list07.setContainerDetails(details);
		getAppLayerFactory().getModelLayerFactory().newRecordServices().update(list07);

		Decommissioner decommissioner = Decommissioner.forList(list07, new DecommissioningService(zeCollection, getAppLayerFactory()), getAppLayerFactory());
		decommissioner.process(list07, records.getAdmin(), LocalDate.now());

		waitForBatchProcess();
		assertThat(records.getContainerBac11().isFull()).isTrue();
	}

	@Test
	public void givenPrioritizingMemoryConsumptionThenReturnMaxRecordsPerTransactionMemory() {
		givenConfig(ConstellioEIMConfigs.MEMORY_CONSUMPTION_LEVEL, LESS_MEMORY_CONSUMPTION);

		DecommissioningList list07 = records.getList17();
		Decommissioner decommissioner = Decommissioner.forList(list07, new DecommissioningService(zeCollection, getAppLayerFactory()), getAppLayerFactory());
		assertThat(decommissioner.getMaxRecordsPerTransaction()).isEqualTo(100);
	}

	@Test
	public void givenNormalThenReturnMaxRecordsPerTransactionNormal() {
		givenConfig(ConstellioEIMConfigs.MEMORY_CONSUMPTION_LEVEL, NORMAL);

		DecommissioningList list07 = records.getList17();
		Decommissioner decommissioner = Decommissioner.forList(list07, new DecommissioningService(zeCollection, getAppLayerFactory()), getAppLayerFactory());
		assertThat(decommissioner.getMaxRecordsPerTransaction()).isEqualTo(500);
	}

	@Test
	public void givenPrioritizingPerformanceThenReturnMaxRecordsPerTransactionPerformance() {
		givenConfig(ConstellioEIMConfigs.MEMORY_CONSUMPTION_LEVEL, BETTER_PERFORMANCE);

		DecommissioningList list07 = records.getList17();
		Decommissioner decommissioner = Decommissioner.forList(list07, new DecommissioningService(zeCollection, getAppLayerFactory()), getAppLayerFactory());
		assertThat(decommissioner.getMaxRecordsPerTransaction()).isEqualTo(1000);
	}

	@Test
	public void givenTransactionRecordCountUnderLimitThenExecuteSynchronously() throws Exception {
		givenConfig(ConstellioEIMConfigs.MEMORY_CONSUMPTION_LEVEL, LESS_MEMORY_CONSUMPTION);

		DecommissioningList list24 = records.getList24();
		ClosingDecommissioner closingDecommissioner = new ClosingDecommissioner(new DecommissioningService(zeCollection, getAppLayerFactory()), getAppLayerFactory());
		LocalDate now = LocalDate.now();

		closingDecommissioner.process(list24, records.getAdmin(), now);
		waitForBatchProcess();

		assertThat(records.getFolder_A01().getCloseDateEntered()).isEqualTo(now);
	}

	@Test
	public void givenTransactionRecordCountOverLimitThenExecuteAsynchronously() throws Exception {
		givenConfig(ConstellioEIMConfigs.MEMORY_CONSUMPTION_LEVEL, LESS_MEMORY_CONSUMPTION);

		DecommissioningList list24 = records.getList24();
		ClosingDecommissioner closingDecommissioner = spy(new ClosingDecommissioner(new DecommissioningService(zeCollection, getAppLayerFactory()), getAppLayerFactory()));
		LocalDate now = LocalDate.now();
		doReturn(0).when(closingDecommissioner).getMaxRecordsPerTransaction();

		closingDecommissioner.process(list24, records.getAdmin(), now);
		waitForBatchProcess();

		assertThat(records.getFolder_A01().getCloseDateEntered()).isEqualTo(now);
	}
}

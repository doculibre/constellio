package com.constellio.model.services.batch.controller;

import com.constellio.data.dao.dto.records.TransactionDTO;
import com.constellio.data.dao.services.DataStoreTypesFactory;
import com.constellio.data.dao.services.bigVault.RecordDaoException;
import com.constellio.data.dao.services.idGenerator.UniqueIdGenerator;
import com.constellio.data.dao.services.records.RecordDao;
import com.constellio.model.entities.batchprocess.BatchProcess;
import com.constellio.model.entities.batchprocess.BatchProcessAction;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.services.batch.actions.ChangeValueOfMetadataBatchProcessAction;
import com.constellio.model.services.batch.manager.BatchProcessesManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.AddToBatchProcessImpactHandler;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.RecordServicesImpl;
import com.constellio.model.services.records.cache.RecordsCaches;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.TestRecord;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.constellio.model.entities.schemas.Schemas.IDENTIFIER;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromAllSchemasIn;
import static junit.framework.Assert.fail;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

//@SlowTest
public class BatchProcessControllerAcceptanceTest extends ConstellioTest {

	String anotherSchemaRecordText = "this is a text";
	String anotherSchemaRecordNewText = "this is an other text";

	RecordsCaches recordsCaches;
	RecordDao recordDao;
	RecordDao eventsDao;
	RecordDao searchDao;
	RecordDao notificationsDao;
	RecordServicesImpl recordServices;
	BatchProcessControllerAcceptanceTestSchemasSetup schemas = new BatchProcessControllerAcceptanceTestSchemasSetup();
	BatchProcessControllerAcceptanceTestSchemasSetup.ZeSchemaMetadatas zeSchema = schemas.new ZeSchemaMetadatas();
	BatchProcessControllerAcceptanceTestSchemasSetup.AnotherSchemaMetadatas anotherSchema = schemas.new AnotherSchemaMetadatas();
	BatchProcessControllerAcceptanceTestSchemasSetup.ThirdSchemaMetadatas thirdSchema = schemas.new ThirdSchemaMetadatas();
	// BatchProcessController controller;
	BatchProcessesManager batchProcessManager;

	ModelLayerFactory modelFactory;

	Record zeSchemaRecord;
	List<String> recordIds;

	@Before
	public void setUp()
			throws Exception {

		cacheIntegrityCheckedAfterTest = false;
		withSpiedServices(ModelLayerFactory.class);

		recordsCaches = getModelLayerFactory().getRecordsCaches();
		eventsDao = spy(getDataLayerFactory().newEventsDao());
		recordDao = spy(getDataLayerFactory().newRecordDao());
		notificationsDao = spy(getDataLayerFactory().newNotificationsDao());
		DataStoreTypesFactory typesFactory = getDataLayerFactory().newTypesFactory();
		UniqueIdGenerator uniqueIdGenerator = getDataLayerFactory().getUniqueIdGenerator();
		recordServices = spy(new RecordServicesImpl(recordDao, eventsDao, searchDao, notificationsDao, getModelLayerFactory(), typesFactory,
				uniqueIdGenerator, recordsCaches));

		modelFactory = getModelLayerFactory();
		batchProcessManager = modelFactory.getBatchProcessesManager();
		// controller = getModelLayerFactory().getBatchProcessesController();
		// controller.start();

		defineSchemasManager().using(schemas.withCopiedTextMetadataFromAnotherSchema().withATitle().withAStringMetadata());

		zeSchemaRecord = createZeSchemaFirstRecord();
		recordIds = addAnotherSchemaAndThirdSchemaRecords(zeSchemaRecord);

		SearchServices searchServices = getModelLayerFactory().newSearchServices();
		doReturn(new AddToBatchProcessImpactHandler(batchProcessManager, searchServices)).when(recordServices)
				.addToBatchProcessModificationImpactHandler();

		givenWaitForBatchProcessAfterTestIsDisabled();
	}

	// @After
	// public void tearDown()
	// throws InterruptedException {
	// controller.clear();
	// }

	@Test
	public void whenReindexingCopiedValueInOneTransactionThenReindexedForAllRecords()
			throws Exception {

		zeSchemaRecord.set(zeSchema.text(), anotherSchemaRecordNewText);
		recordServices.update(zeSchemaRecord);

		assertThatAllRecordsHaveCorrectTextAndLength(recordIds, anotherSchemaRecordNewText);
	}

	@Test
	public void whenReindexingCopiedValueInBatchProcessThenReindexedForAllRecords()
			throws Exception {

		zeSchemaRecord.set(zeSchema.text(), anotherSchemaRecordNewText);
		List<BatchProcess> batchProcesses = recordServices.updateAsync(zeSchemaRecord);
		waitForBatchProcess();

		assertThat(batchProcesses).hasSize(1);
		assertThatAllRecordsHaveCorrectTextAndLength(recordIds, anotherSchemaRecordNewText);
	}

	@Test
	public void givenSystemErrorWhenAddingBatchProcessThenNoModifications()
			throws Exception {

		batchProcessManager = spy(batchProcessManager);
		doThrow(Error.class).when(batchProcessManager)
				.addBatchProcessInStandby(any(LogicalSearchCondition.class), any(BatchProcessAction.class), anyString());
		when(modelFactory.getBatchProcessesManager()).thenReturn(batchProcessManager);
		recordServices = new RecordServicesImpl(recordDao, eventsDao, searchDao, notificationsDao, modelFactory,
				getDataLayerFactory().newTypesFactory(), getDataLayerFactory().getUniqueIdGenerator(), recordsCaches);

		zeSchemaRecord.set(zeSchema.text(), anotherSchemaRecordNewText);
		try {
			recordServices.updateAsync(zeSchemaRecord);
			fail("Error expected");
		} catch (Error e) {
		}

		assertThat(batchProcessManager.getCurrentBatchProcess()).isNull();
		assertThat(batchProcessManager.getPendingBatchProcesses()).isEmpty();
		assertThat(batchProcessManager.getFinishedBatchProcesses()).isEmpty();
		assertThat(recordServices.getDocumentById(zeSchemaRecord.getId()).<String>get(zeSchema.text()))
				.isEqualTo(anotherSchemaRecordText);
		assertThatAllRecordsHaveCorrectTextAndLength(recordIds, anotherSchemaRecordText);
	}

	@Test
	public void givenSystemErrorWhenExecutingTransactionDTOThenNoModifications()
			throws Exception {

		doThrow(Error.class).when(recordDao).execute(any(TransactionDTO.class));

		zeSchemaRecord.set(zeSchema.text(), anotherSchemaRecordNewText);
		try {
			recordServices.updateAsync(zeSchemaRecord);
			fail("Error expected");
		} catch (Error e) {

		}

		assertThat(batchProcessManager.getCurrentBatchProcess()).isNull();
		assertThat(batchProcessManager.getPendingBatchProcesses()).isEmpty();
		assertThat(batchProcessManager.getFinishedBatchProcesses()).isEmpty();
		assertThat(batchProcessManager.getStandbyBatchProcesses()).hasSize(1);
		assertThat(recordServices.getDocumentById(zeSchemaRecord.getId()).<String>get(zeSchema.text()))
				.isEqualTo(anotherSchemaRecordText);
		assertThatAllRecordsHaveCorrectTextAndLength(recordIds, anotherSchemaRecordText);
	}

	@Test
	public void givenMultipleOptimisticLockingThenSystemErrorWhenExecutingTransactionDTOThenNoModificationsAndOnlyOneStandbyBatchProcess()
			throws Exception {

		givenWaitForBatchProcessAfterTestIsDisabled();
		// A really bad day...
		doThrow(RecordDaoException.OptimisticLocking.class).doThrow(RecordDaoException.OptimisticLocking.class)
				.doThrow(Error.class).when(recordDao).execute(any(TransactionDTO.class));

		zeSchemaRecord.set(zeSchema.text(), anotherSchemaRecordNewText);
		try {
			recordServices.updateAsync(zeSchemaRecord);
			fail("Error expected");
		} catch (Error e) {

		}

		assertThat(batchProcessManager.getCurrentBatchProcess()).isNull();
		assertThat(batchProcessManager.getPendingBatchProcesses()).isEmpty();
		assertThat(batchProcessManager.getFinishedBatchProcesses()).isEmpty();
		assertThat(batchProcessManager.getStandbyBatchProcesses()).hasSize(1);
		assertThat(recordServices.getDocumentById(zeSchemaRecord.getId()).<String>get(zeSchema.text()))
				.isEqualTo(anotherSchemaRecordText);
		assertThatAllRecordsHaveCorrectTextAndLength(recordIds, anotherSchemaRecordText);

	}

	@Test
	public void givenMultipleOptimisticLockingWhenExecutingTransactionDTOThenOnlyOneStandbyBatchProcess()
			throws Exception {

		// A bad day...
		doThrow(RecordDaoException.OptimisticLocking.class).doThrow(RecordDaoException.OptimisticLocking.class)
				.doCallRealMethod().when(recordDao).execute(any(TransactionDTO.class));

		zeSchemaRecord.set(zeSchema.text(), anotherSchemaRecordNewText);
		List<BatchProcess> batchProcesses = recordServices.updateAsync(zeSchemaRecord);

		assertThat(batchProcesses).hasSize(1);
		assertThat(batchProcessManager.getAllBatchProcessesCount()).isEqualTo(1);

		waitForBatchProcess();
		assertThatAllRecordsHaveCorrectTextAndLength(recordIds, anotherSchemaRecordNewText);
	}

	@Test
	public void whenChangingValueInOneTransactionThenChangedForAllRecords()
			throws Exception {

		Record zeSchemaRecord1 = createZeSchemaRecordWithTitleAndString();
		Record zeSchemaRecord2 = createZeSchemaRecordWithTitleAndString();
		List<String> recordIds = new ArrayList<>();
		recordIds.add(zeSchemaRecord1.getId());
		recordIds.add(zeSchemaRecord2.getId());

		Map<String, Object> changedMetadataValues = new HashMap<>();
		changedMetadataValues.put(zeSchema.title().getCode(), "changedTitle");
		changedMetadataValues.put(zeSchema.stringMetadata().getCode(), "changedString");

		BatchProcessAction action = new ChangeValueOfMetadataBatchProcessAction(changedMetadataValues);

		LogicalSearchCondition condition = fromAllSchemasIn(zeCollection).where(IDENTIFIER).isIn(recordIds);
		BatchProcess batchProcess = batchProcessManager.addBatchProcessInStandby(condition, action, "zeTitle");
		batchProcessManager.markAsPending(batchProcess);

		waitForBatchProcess();

		for (Record record : recordServices.getRecordsById(zeCollection, recordIds)) {
			assertThat(record.<String>get(zeSchema.title())).isEqualTo("changedTitle");
			assertThat(record.<String>get(zeSchema.stringMetadata())).isEqualTo("changedString");
		}

	}

	private Record createZeSchemaRecordWithTitleAndString()
			throws RecordServicesException {
		Record zeSchemaRecord = recordServices.newRecordWithSchema(zeSchema.instance());
		zeSchemaRecord.set(zeSchema.title(), "initialTitle");
		zeSchemaRecord.set(zeSchema.stringMetadata(), "initialString");
		recordServices.add(zeSchemaRecord);
		return zeSchemaRecord;
	}

	private List<String> addAnotherSchemaAndThirdSchemaRecords(Record zeSchemaRecord)
			throws Exception {

		List<String> recordIds = new ArrayList<>();
		Transaction transaction = new Transaction();
		for (int i = 0; i < 30; i++) {
			Record anotherSchemaRecord = new TestRecord(anotherSchema, "_" + (i + 1));
			anotherSchemaRecord.set(anotherSchema.referenceToZeSchema(), zeSchemaRecord.getId());
			transaction.addUpdate(anotherSchemaRecord);

			for (int j = 0; j < 10; j++) {
				Record thirdSchemaRecord = new TestRecord(thirdSchema, anotherSchemaRecord.getId() + "_" + (i + 1));
				thirdSchemaRecord.set(thirdSchema.referenceToAnotherSchema(), anotherSchemaRecord.getId());
				transaction.addUpdate(thirdSchemaRecord);
			}
		}

		recordServices.execute(transaction);

		recordIds.addAll(transaction.getRecordIds());

		assertThatAllRecordsHaveCorrectTextAndLength(recordIds, anotherSchemaRecordText);

		return recordIds;
	}

	private void assertThatAllRecordsHaveCorrectTextAndLength(List<String> recordIds, String text) {
		for (String recordId : recordIds) {
			Record record = recordServices.getDocumentById(recordId);

			if (record.getSchemaCode().equals(anotherSchema.code())) {
				assertThat(record.<String>get(anotherSchema.copiedText())).isEqualTo(text);
				assertThat(record.<Double>get(anotherSchema.copiedTextLength())).isEqualTo(Double.valueOf(text.length()));
			} else {
				assertThat(record.<String>get(thirdSchema.copiedText())).isEqualTo(text);
				assertThat(record.<Double>get(thirdSchema.copiedTextLength())).isEqualTo(Double.valueOf(text.length()));
			}
		}
	}

	private Record createZeSchemaFirstRecord()
			throws RecordServicesException {
		Record anotherSchemaFirstRecord = new TestRecord(zeSchema);
		anotherSchemaFirstRecord.set(zeSchema.text(), anotherSchemaRecordText);
		recordServices.add(anotherSchemaFirstRecord);
		return anotherSchemaFirstRecord;
	}

}

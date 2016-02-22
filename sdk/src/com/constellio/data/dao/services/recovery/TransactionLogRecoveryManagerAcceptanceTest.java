package com.constellio.data.dao.services.recovery;

import static com.constellio.app.modules.rm.model.enums.CopyType.PRINCIPAL;
import static com.constellio.sdk.tests.SaveStateFeatureAcceptTest.verifySameContentOfUnzippedSaveState;
import static junit.framework.TestCase.fail;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;

import org.joda.time.LocalDateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.RecordServicesRuntimeException.NoSuchRecordWithId;
import com.constellio.sdk.tests.ConstellioTest;
import com.sun.star.uno.RuntimeException;

public class TransactionLogRecoveryManagerAcceptanceTest extends ConstellioTest {
	RMTestRecords records = new RMTestRecords(zeCollection);
	RecordServices recordServices;
	TransactionLogRecoveryManager transactionLogRecoveryManager;
	String recordNotModifiedId, existingRecordToBeDeletedLogicallyId, existingRecordToBeDeletedPhysicallyId,
			recordCreatedId, recordCreatedToBeDeletedLogicallyId,
			recordCreatedToBeDeletedPhysicallyId, recordModifiedId;
	private RMSchemasRecordsServices rm;
	private LocalDateTime beforeStartRollBack = LocalDateTime.now().plusDays(7);

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	@Before
	public void setUp()
			throws Exception {
		givenTransactionLogIsEnabled();
		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers().withRMTest(records)
						.withFoldersAndContainersOfEveryStatus()
		);
		transactionLogRecoveryManager = spy(getDataLayerFactory().getTransactionLogRecoveryManager());
		rm = new RMSchemasRecordsServices(zeCollection, getModelLayerFactory());
		recordServices = getModelLayerFactory().newRecordServices();
	}

	@Test
	public void whenIsInRollbackModeThenOk()
			throws Exception {
		assertThat(transactionLogRecoveryManager.isInRollbackMode()).isFalse();
		transactionLogRecoveryManager.startRollbackMode();
		assertThat(transactionLogRecoveryManager.isInRollbackMode()).isTrue();
		transactionLogRecoveryManager.rollback(null);
		assertThat(transactionLogRecoveryManager.isInRollbackMode()).isFalse();
		transactionLogRecoveryManager.startRollbackMode();
		assertThat(transactionLogRecoveryManager.isInRollbackMode()).isTrue();
		transactionLogRecoveryManager.stopRollbackMode();
		assertThat(transactionLogRecoveryManager.isInRollbackMode()).isFalse();
	}

	@Test
	public void givenNotInRollBackModeThenNoCallForStopRollbackNorForRollbackButStart()
			throws Exception {
		transactionLogRecoveryManager.stopRollbackMode();
		verify(transactionLogRecoveryManager, times(0)).realStopRollback();

		Throwable t = new RuntimeException();
		transactionLogRecoveryManager.rollback(t);
		verify(transactionLogRecoveryManager, times(0)).realRollback(t);

		transactionLogRecoveryManager.startRollbackMode();
		verify(transactionLogRecoveryManager, times(1)).realStartRollback();
	}

	@Test
	public void givenInRollBackModeThenNoCallForStartButStopRollbackAndRollbackYes()
			throws Exception {
		transactionLogRecoveryManager.startRollbackMode();
		transactionLogRecoveryManager.startRollbackMode();
		verify(transactionLogRecoveryManager, times(1)).realStartRollback();

		transactionLogRecoveryManager.stopRollbackMode();
		verify(transactionLogRecoveryManager, times(1)).realStopRollback();

		transactionLogRecoveryManager.startRollbackMode();
		Throwable t = new RuntimeException();
		transactionLogRecoveryManager.rollback(t);
		verify(transactionLogRecoveryManager, times(1)).realRollback(t);
	}

	@Test
	public void givenRollBackStartedAndSomeModificationWhenStopRollbackThenAllModificationsSaved()
			throws Exception {
		givenTimeIs(beforeStartRollBack);
		initTestRecords();

		givenTimeIs(beforeStartRollBack.plusDays(1));
		transactionLogRecoveryManager.startRollbackMode();

		someModification();
		transactionLogRecoveryManager.stopRollbackMode();
		validateRecordsModifiedAsExpected();
	}

	@After
	public void afterTest() {
		getDataLayerFactory().getRecordsVaultServer().unregisterListener(transactionLogRecoveryManager);
	}

	@Test
	public void givenRollBackStartedAndSomeModificationWhenRollbackThenSameStateAsBeforeStartingRollback()
			throws Exception {
		givenTimeIs(beforeStartRollBack);
		initTestRecords();

		File state1 = folder.newFile("state1.zip");
		File state2 = folder.newFile("state2.zip");

		givenTimeIs(beforeStartRollBack.plusDays(1));
		getSaveStateFeature().saveCurrentStateTo(state1);
		transactionLogRecoveryManager.startRollbackMode();

		someModification();
		transactionLogRecoveryManager.rollback(null);
		getSaveStateFeature().saveCurrentStateTo(state2);
		assertThatStatesAreEqual(state1, state2);
	}

	private void assertThatStatesAreEqual(File state1, File state2)
			throws Exception {
		File state1TempFolder = newTempFolder();
		File state2TempFolder = newTempFolder();

		getIOLayerFactory().newZipService().unzip(state1, state1TempFolder);
		getIOLayerFactory().newZipService().unzip(state2, state2TempFolder);

		verifySameContentOfUnzippedSaveState(state1TempFolder, state2TempFolder);
	}

	private void initTestRecords()
			throws RecordServicesException {
		initTestRecordsIds();
		Folder folderNotModified = rm.getFolder(recordNotModifiedId);
		execute(folderNotModified.setTitle("mm").setModifiedOn(beforeStartRollBack).getWrappedRecord());
	}

	private void initTestRecordsIds() {
		recordNotModifiedId = records.getFolder_A01().getId();
		existingRecordToBeDeletedLogicallyId = records.getFolder_A02().getId();
		existingRecordToBeDeletedPhysicallyId = records.getFolder_A05().getId();
		recordModifiedId = records.getFolder_A04().getId();
	}

	private void someModification()
			throws RecordServicesException {
		modifyRecord(recordModifiedId);
		createNewRecords();
		modifyRecord(recordCreatedId);
		deleteLogically(recordCreatedToBeDeletedLogicallyId);
		deletePhysically(recordCreatedToBeDeletedPhysicallyId);
		deleteLogically(existingRecordToBeDeletedLogicallyId);
		deletePhysically(existingRecordToBeDeletedPhysicallyId);
	}

	private void deletePhysically(String recordId) {
		recordServices.logicallyDelete(recordServices.getDocumentById(recordId), null);
		recordServices.physicallyDelete(recordServices.getDocumentById(recordId), null);
	}

	private void deleteLogically(String recordId) {
		recordServices.logicallyDelete(recordServices.getDocumentById(recordId), null);
	}

	private void createNewRecords()
			throws RecordServicesException {
		Folder recordCreated = rm.newFolder();
		recordCreatedId = recordCreated.getId();
		Folder recordCreatedToBeDeletedLogically = rm.newFolder();
		recordCreatedToBeDeletedLogicallyId = recordCreatedToBeDeletedLogically.getId();
		Folder recordCreatedToBeDeletedPhysically = rm.newFolder();
		recordCreatedToBeDeletedPhysicallyId = recordCreatedToBeDeletedPhysically.getId();
		Transaction transaction = new Transaction();
		transaction.add(recordCreated.setTitle("created")
				.setAdministrativeUnitEntered(records.unitId_10a)
				.setCategoryEntered(records.categoryId_X).setRetentionRuleEntered(records.ruleId_2)
				.setMediumTypes(rm.PA(), rm.DM()).setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 10, 4))
				.getWrappedRecord());
		transaction.add(recordCreatedToBeDeletedLogically.setTitle("to be deleted l")
				.setAdministrativeUnitEntered(records.unitId_10a)
				.setCategoryEntered(records.categoryId_X).setRetentionRuleEntered(records.ruleId_2)
				.setMediumTypes(rm.PA(), rm.DM()).setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 10, 4))
				.getWrappedRecord());
		transaction.add(recordCreatedToBeDeletedPhysically.setTitle("to be deleted p")
				.setAdministrativeUnitEntered(records.unitId_10a)
				.setCategoryEntered(records.categoryId_X).setRetentionRuleEntered(records.ruleId_2)
				.setMediumTypes(rm.PA(), rm.DM()).setCopyStatusEntered(PRINCIPAL).setOpenDate(date(2000, 10, 4))
				.getWrappedRecord());
		recordServices.execute(transaction);
	}

	private void modifyRecord(String recordId)
			throws RecordServicesException {
		execute(recordServices.getDocumentById(recordId).set(Schemas.TITLE, getClass().getName() + recordId));
	}

	private void execute(Record folder)
			throws RecordServicesException {
		Transaction transaction = new Transaction();
		transaction.add(folder);
		recordServices.execute(transaction);
	}

	private void validateRecordsModifiedAsExpected() {
		Folder folderNotModified = rm.getFolder(recordNotModifiedId);
		assertThat(folderNotModified.getModifiedOn()).isEqualTo(beforeStartRollBack);

		Folder modifiedRecord = rm.getFolder(recordModifiedId);
		assertThat(modifiedRecord.getTitle()).isEqualTo(getClass().getName() + recordModifiedId);

		Folder createdRecord = rm.getFolder(recordCreatedId);
		assertThat(createdRecord.getTitle()).isEqualTo(getClass().getName() + recordCreatedId);

		Folder folderDeltedLogically = rm.getFolder(recordCreatedToBeDeletedLogicallyId);
		assertThat(folderDeltedLogically.isLogicallyDeletedStatus()).isTrue();

		try {
			rm.getFolder(recordCreatedToBeDeletedPhysicallyId);
			fail("Expecting record to be deleted physically " + recordCreatedToBeDeletedPhysicallyId);
		} catch (NoSuchRecordWithId e) {
			//ok
		}

		Folder existingFolderDeltedLogically = rm.getFolder(existingRecordToBeDeletedLogicallyId);
		assertThat(existingFolderDeltedLogically.isLogicallyDeletedStatus()).isTrue();

		try {
			rm.getFolder(existingRecordToBeDeletedPhysicallyId);
			fail("Expecting record to be deleted physically " + existingRecordToBeDeletedPhysicallyId);
		} catch (NoSuchRecordWithId e) {
			//ok
		}
	}

	/*
	void startRollbackMode();
	void stopRollbackMode();
	void rollback(Throwable t);
	boolean isInRollbackMode();
	* */

}

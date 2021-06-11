package com.constellio.app.services.recovery;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.data.dao.managers.config.values.TextConfiguration;
import com.constellio.data.dao.services.records.RecordDao;
import com.constellio.data.conf.FoldersLocator;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.RecordServicesRuntimeException.NoSuchRecordWithId;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.MetadataSchemasManagerException.OptimisticLocking;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.SolrSDKToolsServices;
import com.constellio.sdk.tests.SolrSDKToolsServices.VaultSnapshot;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static com.constellio.app.modules.rm.model.enums.CopyType.PRINCIPAL;
import static com.constellio.app.services.recovery.UpdateRecoveryImpossibleCause.TOO_SHORT_MEMORY;
import static com.constellio.app.services.recovery.UpgradeAppRecoveryServiceImpl.REQUIRED_MEMORY_IN_MO;
import static com.constellio.app.services.recovery.UpgradeAppRecoveryServiceImpl.REQUIRED_SPACE_IN_GIG;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class UpgradeAppRecoveryServiceImplAcceptanceTest extends ConstellioTest {
	RMTestRecords records = new RMTestRecords(zeCollection);
	RecordServices recordServices;
	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	UpgradeAppRecoveryServiceImpl upgradeAppRecoveryService;
	String recordNotModifiedId, existingRecordToBeDeletedLogicallyId, existingRecordToBeDeletedPhysicallyId,
			recordCreatedId, recordCreatedToBeDeletedLogicallyId,
			recordCreatedToBeDeletedPhysicallyId, recordModifiedId;
	private RMSchemasRecordsServices rm;
	private LocalDateTime beforeStartRollBack = LocalDateTime.now().plusDays(7);
	private String addedSchemaTypeCode = "UpgradeAppRecoveryServiceImplAcceptanceTestLol";

	@Before
	public void setUp()
			throws Exception {
		withSpiedServices(FoldersLocator.class);
		givenTransactionLogIsEnabled();
		givenDisabledAfterTestValidations();
		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTestUsers().withRMTest(records)
						.withFoldersAndContainersOfEveryStatus()
		);
		upgradeAppRecoveryService = new UpgradeAppRecoveryServiceImpl(getAppLayerFactory(), getIOLayerFactory().newIOServices());
		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		recordServices = getModelLayerFactory().newRecordServices();
	}

	@Test
	public void whenIsValidWarThenBehavesAsExpected()
			throws Exception {
		REQUIRED_MEMORY_IN_MO = 1;
		REQUIRED_SPACE_IN_GIG = 1;
		assertThat(upgradeAppRecoveryService.isUpdateWithRecoveryPossible()).isNull();

		REQUIRED_MEMORY_IN_MO = 1000 * 1024;
		assertThat(upgradeAppRecoveryService.isUpdateWithRecoveryPossible()).isEqualTo(TOO_SHORT_MEMORY);

	}

	//@Test
	public void givenRollBackStartedAndSomeModificationWhenRollbackThenSameStateAsBeforeStartingRollback()
			throws Exception {
		givenTimeIs(beforeStartRollBack);
		initTestRecords();

		RecordDao recordDao = getDataLayerFactory().newRecordDao();
		SolrSDKToolsServices tools = new SolrSDKToolsServices(recordDao);

		givenTimeIs(beforeStartRollBack.plusDays(1));
		VaultSnapshot snapshotBeforeReplay = tools.snapshot();

		upgradeAppRecoveryService.startRollbackMode();
		someModification();
		upgradeAppRecoveryService.rollback(null);

		VaultSnapshot currentSnapShot = tools.snapshot();
		tools.ensureSameSnapshots("", snapshotBeforeReplay, currentSnapShot);
		TextConfiguration schemas = getDataLayerFactory().getConfigManager()
				.getText(zeCollection + "/schemas.xml");
		assertThat(schemas.getText().contains(addedSchemaTypeCode)).isFalse();
	}

	@Test
	public void givenRollBackStartedAndSomeModificationWhenStopRollbackThenAllModificationsSaved()
			throws Exception {
		givenTimeIs(beforeStartRollBack);
		initTestRecords();

		givenTimeIs(beforeStartRollBack.plusDays(1));
		upgradeAppRecoveryService.startRollbackMode();

		someModification();
		upgradeAppRecoveryService.stopRollbackMode();
		validateRecordsModifiedAsExpected();
		TextConfiguration schemas = getDataLayerFactory().getConfigManager()
				.getText(zeCollection + "/schemas.xml");
		assertThat(schemas.getText().contains(addedSchemaTypeCode)).isTrue();
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
		addSchema(zeCollection, addedSchemaTypeCode);
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

	private void addSchema(String collection, String schemaCode) {
		MetadataSchemasManager schemaManager = getAppLayerFactory()
				.getModelLayerFactory().getMetadataSchemasManager();
		MetadataSchemaTypesBuilder types = schemaManager.modify(collection);
		types.createNewSchemaTypeWithSecurity(schemaCode);
		try {
			schemaManager.saveUpdateSchemaTypes(types);
		} catch (OptimisticLocking optimistickLocking) {
			throw new java.lang.RuntimeException(optimistickLocking);
		}
		getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection).getSchemaType(addedSchemaTypeCode);
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
}

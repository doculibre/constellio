package com.constellio.app.modules.rm.configScripts;

import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.RMFolderCopyStatusPriorityScript;
import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.model.enums.CopyType;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class RMFolderCopyStatusPriorityScriptAcceptanceTest extends ConstellioTest {

	RMTestRecords records;
	RMSchemasRecordsServices rm;
	RecordServices recordServices;
	RMFolderCopyStatusPriorityScript copyStatusPriorityScript;
	ValidationErrors errors;

	@Before
	public void setUp()
			throws Exception {
		records = new RMTestRecords(zeCollection);
		prepareSystem(
				withZeCollection().withConstellioRMModule().withRMTest(records)
						.withFoldersAndContainersOfEveryStatus()
		);
		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		recordServices = getAppLayerFactory().getModelLayerFactory().newRecordServices();
		copyStatusPriorityScript = new RMFolderCopyStatusPriorityScript();
		errors = new ValidationErrors();
	}

	@Test
	public void whenEnablingCopyStatusEnteredPriorityWithNoSubfolderWithCopyStatusEnteredThenSucceeds() {
		copyStatusPriorityScript.validate(true, errors);
		assertThat(errors.getValidationErrors()).hasSize(0);
	}

	@Test
	public void whenEnablingCopyStatusEnteredPriorityWithSubfolderWithCopyStatusEnteredThenFails()
			throws RecordServicesException {
		// Used to stop RMFolderExtension from setting the copy status entered has null
		givenConfig(RMConfigs.COPY_STATUS_ENTERED_HAS_PRIORITY_OVER_PARENTS_COPY_STATUS, true);

		recordServices.execute(new Transaction(
				rm.getFolder(records.folder_A02)
						.setCopyStatusEntered(CopyType.SECONDARY)
						.setParentFolder(records.folder_A01))
				.setSkippingRequiredValuesValidation(true));

		copyStatusPriorityScript.validate(true, errors);
		assertThat(errors.getValidationErrors()).hasSize(1);
		assertThat(errors.getValidationErrors().get(0).getCode())
				.isEqualTo("com.constellio.app.modules.rm.RMFolderCopyStatusPriorityScript_containsSubfolderWithCopyStatusEntered");
	}

	@Test
	public void whenCopyStatusEnteredPriorityIsEnableThenCalculatorUsesCopyStatusEntered()
			throws Exception {
		givenConfig(RMConfigs.COPY_STATUS_ENTERED_HAS_PRIORITY_OVER_PARENTS_COPY_STATUS, true);

		createDreddFolder();
		assertThat(rm.getFolder("Dredd").getCopyStatus()).isEqualTo(CopyType.SECONDARY);
	}

	@Test
	public void whenCopyStatusEnteredPriorityIsDisableThenCalculatorUsesParentFoldersCopyStatus()
			throws Exception {
		givenConfig(RMConfigs.COPY_STATUS_ENTERED_HAS_PRIORITY_OVER_PARENTS_COPY_STATUS, false);

		createDreddFolder();
		assertThat(rm.getFolder("Dredd").getCopyStatus()).isEqualTo(CopyType.PRINCIPAL);
	}

	private void createDreddFolder() throws RecordServicesException {
		recordServices.add(() -> rm.newFolderWithId("Dredd")
				.setTitle("Judge Dredd")
				.setParentFolder(records.folder_A01)
				.setCopyStatusEntered(CopyType.SECONDARY)
				.setOpenDate(TimeProvider.getLocalDate())
				.getWrappedRecord());
	}
}

package com.constellio.app.modules.rm.model.calculators;

import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.model.services.records.RecordServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class FolderCopyRulesExpectedDestructionDatesCalculatorAcceptanceTest extends ConstellioTest {

	private RMSchemasRecordsServices rm;
	private RMTestRecords records = new RMTestRecords(zeCollection);
	private RecordServices recordServices;
	private Users users = new Users();

	@Before
	public void setUp() throws Exception {
		prepareSystem(withZeCollection().withConstellioRMModule().withAllTest(users).withRMTest(records)
				.withFoldersAndContainersOfEveryStatus().withDocumentsDecommissioningList());

		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		recordServices = getModelLayerFactory().newRecordServices();
	}


	@Test
	public void whenFolderWithActualTransferDateAndConfigNotBasedOnActualTransferDateThenDateIsBasedOnExpectedDate()
			throws Exception {
		givenConfig(RMConfigs.DEPOSIT_AND_DESTRUCTION_DATES_BASED_ON_ACTUAL_TRANSFER_DATE, false);

		Folder folder = rm.wrapFolder(recordServices.getDocumentById("A10"));
		LocalDate expectedDestructionDate = folder.getExpectedDestructionDate();

		folder.setActualTransferDate(new LocalDate(2007, 10, 31));
		recordServices.update(folder);

		reindex();
		waitForBatchProcess();

		assertThat(folder.getExpectedDestructionDate()).isEqualTo(expectedDestructionDate);
	}

	@Test
	public void whenFolderWithActualTransferDateAndConfigBasedOnActualTransferDateThenDateIsNotBasedOnExpectedDate()
			throws Exception {
		givenConfig(RMConfigs.DEPOSIT_AND_DESTRUCTION_DATES_BASED_ON_ACTUAL_TRANSFER_DATE, true);

		Folder folder = rm.wrapFolder(recordServices.getDocumentById("A10"));
		LocalDate expectedDestructionDate = folder.getExpectedDestructionDate();

		folder.setActualTransferDate(new LocalDate(2007, 10, 31));
		recordServices.update(folder);

		reindex();
		waitForBatchProcess();

		assertThat(folder.getExpectedDestructionDate()).isNotEqualTo(expectedDestructionDate);
	}


}

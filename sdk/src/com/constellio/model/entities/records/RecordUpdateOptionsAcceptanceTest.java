package com.constellio.model.entities.records;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException.ValidationException;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class RecordUpdateOptionsAcceptanceTest extends ConstellioTest {

	protected RMTestRecords records = new RMTestRecords(zeCollection);

	protected RMSchemasRecordsServices rm;
	protected RecordServices recordServices;

	@Before
	public void setUp()
			throws Exception {
		prepareSystem(withZeCollection().withConstellioRMModule().withRMTest(records)
				.withFoldersAndContainersOfEveryStatus());

		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		recordServices = getAppLayerFactory().getModelLayerFactory().newRecordServices();
	}

	@Test
	public void givenIllegalCharacterWithReplaceOptionThenOk() throws Exception {
		givenConfig(ConstellioEIMConfigs.ENABLE_ILLEGAL_CHARACTERS_VALIDATION, true);

		Folder folder = rm.getFolder(records.folder_A01);
		folder.setTitle("Title:1");

		Transaction tx = new Transaction();
		tx.addUpdate(folder.getWrappedRecord());

		try {
			recordServices.execute(tx);
			fail("ValidationException expected");
		} catch (ValidationException ignored) {

		}

		tx.setOptions(new RecordUpdateOptions().setReplacingIllegalCharactersIfException(true));

		recordServices.execute(tx);

		folder = rm.getFolder(records.folder_A01);
		assertThat(folder.getTitle()).isEqualTo("Title_1");
	}
}

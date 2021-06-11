package com.constellio.app.modules.rm.migrations;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.services.records.RecordServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class RMMigrationTo9_2_14_AcceptanceTest extends ConstellioTest {
	Users users = new Users();
	RMTestRecords records = new RMTestRecords(zeCollection);
	RecordServices recordServices;
	RMSchemasRecordsServices rm;

	@Before
	public void setUp() {
		prepareSystem(
				withZeCollection().withConstellioRMModule().withAllTest(users).withRMTest(records).withFoldersAndContainersOfEveryStatus().withDocumentsHavingContent()
		);

		this.recordServices = getModelLayerFactory().newRecordServices();
		this.rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
	}

	@Test
	public void givenDocumentWithContentThenIsAvalibleInSummaryCache() {


		Record record = recordServices.getRecordSummaryById(zeCollection, records.document_A19);

		Document document = rm.wrapDocument(record);

		Content content = document.getContent();

		assertThat(content).isNotNull();
		assertThat(content.getCurrentVersion().getVersion()).isEqualTo("1.0");
		assertThat(content.getCurrentVersion().getFilename()).isEqualTo("Chevreuil.odt");
		content.getCurrentVersion();
	}
}

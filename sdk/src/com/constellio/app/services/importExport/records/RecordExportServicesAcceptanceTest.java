package com.constellio.app.services.importExport.records;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.ALL;
import static com.constellio.sdk.tests.TestUtils.assertThatRecords;
import static org.assertj.core.api.Assertions.tuple;

import java.io.File;

import org.junit.Test;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.type.DocumentType;
import com.constellio.app.services.schemas.bulkImport.BulkImportParams;
import com.constellio.app.services.schemas.bulkImport.LoggerBulkImportProgressionListener;
import com.constellio.app.services.schemas.bulkImport.RecordsImportServices;
import com.constellio.app.services.schemas.bulkImport.data.ImportDataProvider;
import com.constellio.app.services.schemas.bulkImport.data.xml.XMLImportDataProvider;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.frameworks.validation.ValidationException;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;

public class RecordExportServicesAcceptanceTest extends ConstellioTest {

	RMTestRecords records = new RMTestRecords(zeCollection);
	RecordExportOptions options = new RecordExportOptions();
	Users users = new Users();

	@Test(expected = RecordExportServicesRuntimeException.ExportServicesRuntimeException_NoRecords.class)
	public void givenEmptyCollectionWhenExportRecordsThenExceptionThrown()
			throws Exception {

		prepareSystem(
				withZeCollection().withConstellioRMModule().withConstellioRMModule().withAllTest(users),
				withCollection("anotherCollection").withConstellioRMModule().withAllTest(users));

		exportThenImportInAnotherCollection(options);

	}

	@Test
	public void whenExportingTaxonomiesThenExported()
			throws Exception {

		prepareSystem(
				withZeCollection().withConstellioRMModule().withConstellioRMModule().withAllTest(users).withRMTest(records),
				withCollection("anotherCollection").withConstellioRMModule().withAllTest(users));

		exportThenImportInAnotherCollection(options.setExportTaxonomies(true));

		RMSchemasRecordsServices rm = new RMSchemasRecordsServices("anotherCollection", getAppLayerFactory());
		assertThatRecords(rm.searchAdministrativeUnits(ALL)).extractingMetadatas("code", "title", "parent.code").containsOnly(
				tuple("10", "Unité 10", null),
				tuple("10-A", "Unité 10-A", "10")
		);

	}

	@Test
	public void whenExportingValueListsThenExported()
			throws Exception {

		prepareSystem(
				withZeCollection().withConstellioRMModule().withConstellioRMModule().withAllTest(users).withRMTest(records),
				withCollection("anotherCollection").withConstellioRMModule().withAllTest(users));

		exportThenImportInAnotherCollection(options.setExportValueLists(true));

		RMSchemasRecordsServices rm = new RMSchemasRecordsServices("anotherCollection", getAppLayerFactory());
		assertThatRecords(rm.searchDocumentTypes(ALL)).extractingMetadatas("code", "title", "linkedSchema").containsOnly(
				tuple(DocumentType.EMAIL_DOCUMENT_TYPE, "Ze email", "document_email")
		);

	}

	// ----------------------------------------------------

	private void exportThenImportInAnotherCollection(RecordExportOptions options) {
		File zipFile = new RecordExportServices(getAppLayerFactory()).exportRecords(zeCollection, SDK_STREAM, options);

		ImportDataProvider importDataProvider = XMLImportDataProvider.forZipFile(getModelLayerFactory(), zipFile);

		UserServices userServices = getModelLayerFactory().newUserServices();
		User user = userServices.getUserInCollection("admin", "anotherCollection");
		BulkImportParams importParams = BulkImportParams.STRICT();
		LoggerBulkImportProgressionListener listener = new LoggerBulkImportProgressionListener();
		try {
			new RecordsImportServices(getModelLayerFactory()).bulkImport(importDataProvider, listener, user, importParams);
		} catch (ValidationException e) {
			throw new RuntimeException(e);
		}
	}
}

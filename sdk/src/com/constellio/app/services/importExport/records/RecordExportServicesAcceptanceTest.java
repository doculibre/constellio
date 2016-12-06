package com.constellio.app.services.importExport.records;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.ALL;
import static com.constellio.sdk.tests.TestUtils.asList;
import static com.constellio.sdk.tests.TestUtils.assertThatRecords;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.Assert.fail;

import java.io.File;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.modules.rm.wrappers.type.DocumentType;
import com.constellio.app.services.schemas.bulkImport.BulkImportParams;
import com.constellio.app.services.schemas.bulkImport.LoggerBulkImportProgressionListener;
import com.constellio.app.services.schemas.bulkImport.RecordsImportServices;
import com.constellio.app.services.schemas.bulkImport.data.ImportDataProvider;
import com.constellio.app.services.schemas.bulkImport.data.xml.XMLImportDataProvider;
import com.constellio.app.ui.i18n.i18n;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.frameworks.validation.ValidationException;
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
	public void whenExportingWithTwoSchemaThenAssert()
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
	public void whenExportingSpecificSchemaTypesThenExported()
			throws Exception {
		prepareSystem(
				withZeCollection().withConstellioRMModule().withConstellioRMModule().withAllTest(users).withRMTest(records),
				withCollection("anotherCollection").withConstellioRMModule().withAllTest(users));

		// Category.SCHEMA_TYPE, RetentionRule.SCHEMA_TYPE
		exportThenImportInAnotherCollection(
				options.setExportedSchemaTypes(
						asList(AdministrativeUnit.SCHEMA_TYPE)));

		RMSchemasRecordsServices rm = new RMSchemasRecordsServices("anotherCollection", getAppLayerFactory());
		assertThatRecords(rm.searchAdministrativeUnits(ALL)).extractingMetadatas("code", "title", "parent.code").containsOnly(
				tuple("10A", "Unité 10-A", "10"), tuple("11B", "Unité 11-B", "11"), tuple("11", "Unité 11", "10"),
				tuple("12", "Unité 12", "10"), tuple("20", "Unité 20", null), tuple("30", "Unité 30", null),
				tuple("10", "Unité 10", null), tuple("30C", "Unité 30-C", "30"), tuple("12B", "Unité 12-B", "12"),
				tuple("12C", "Unité 12-C", "12"), tuple("20D", "Unité 20-D", "20"), tuple("20E", "Unité 20-E", "20")
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

			fail(StringUtils.join(i18n.asListOfMessages(e.getValidationErrors()), "\n"));

		}
	}

	private void exportThen(RecordExportOptions options) {
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
}

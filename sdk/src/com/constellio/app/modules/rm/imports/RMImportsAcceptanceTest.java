package com.constellio.app.modules.rm.imports;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.services.schemas.bulkImport.BulkImportProgressionListener;
import com.constellio.app.services.schemas.bulkImport.LoggerBulkImportProgressionListener;
import com.constellio.app.services.schemas.bulkImport.RecordsImportServices;
import com.constellio.app.services.schemas.bulkImport.data.ImportDataProvider;
import com.constellio.app.services.schemas.bulkImport.data.xml.XMLImportDataProvider;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.sdk.tests.ConstellioTest;

public class RMImportsAcceptanceTest extends ConstellioTest {

	RecordsImportServices importServices;
	RMSchemasRecordsServices rm;

	@Test
	public void whenImportingDocumentTypesAndRetentionRulesAtTheSameMomentThenOK()
			throws Exception {

		prepareSystem(
				withZeCollection().withAllTestUsers().withConstellioRMModule()
		);
		importServices = new RecordsImportServices(getModelLayerFactory(), 1);
		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());

		ImportDataProvider importDataProvider = XMLImportDataProvider.forZipFile(
				getModelLayerFactory(), getTestResourceFile("data.zip"));

		BulkImportProgressionListener progressionListener = new LoggerBulkImportProgressionListener();

		User admin = getModelLayerFactory().newUserServices().getUserInCollection("admin", zeCollection);

		importServices.bulkImport(importDataProvider, progressionListener, admin);

		RetentionRule rule1 = rm.getRetentionRuleByCode("111200");
		assertThat(rule1.getDocumentTypesDetails()).isNotEmpty();
		assertThat(rule1.getDocumentTypes()).isNotEmpty();

	}
}

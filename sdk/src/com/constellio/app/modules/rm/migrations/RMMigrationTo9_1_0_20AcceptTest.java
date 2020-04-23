package com.constellio.app.modules.rm.migrations;

import com.constellio.app.services.metadata.AppSchemasServices;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.schemas.MetadataSchemasRuntimeException;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.MetadataSchemasManagerException.OptimisticLocking;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

public class RMMigrationTo9_1_0_20AcceptTest extends ConstellioTest {
	private AppSchemasServices appSchemasServices;
	private SchemasDisplayManager schemasDisplayManager;
	private MetadataSchemasManager schemasManager;
	private final static String CUSTOM_SCHEMA_NAME = "USRcustomSchemaCode";


	@Before
	public void setUp() throws Exception {
		prepareSystem(
				withZeCollection().withConstellioRMModule()
		);
		appSchemasServices = getAppLayerFactory().newSchemasServices();
		schemasManager = getModelLayerFactory().getMetadataSchemasManager();
		schemasDisplayManager = getAppLayerFactory().getMetadataSchemasDisplayManager();
	}


	@Test
	public void givenNewCustomFolderSchemaWithNoRecordsThenCanBeDeleted()
			throws RecordServicesException, OptimisticLocking {
		//create schema
		String schemaCode = "folder_" + CUSTOM_SCHEMA_NAME;
		Map<String, String> labels = new HashMap<>();
		MetadataSchemaTypesBuilder types = schemasManager.modify(zeCollection);
		types.getSchemaType("folder").createCustomSchema(CUSTOM_SCHEMA_NAME, labels);
		schemasManager.saveUpdateSchemaTypes(types);
		schemasDisplayManager.saveSchema(schemasDisplayManager.getSchema(zeCollection, schemaCode)
				.withDisplayMetadataCodes(asList("title")));

		//confirm exists
		try {
			schemasDisplayManager.getSchema(zeCollection, schemaCode);
		} catch (MetadataSchemasRuntimeException.NoSuchSchema e) {
			fail("Schema not created");
		}

		//confirm can be deleted
		ValidationErrors deleteValidationErrors = appSchemasServices.isSchemaDeletable(zeCollection, schemaCode);
		assertThat(deleteValidationErrors).isNull();
		appSchemasServices.deleteSchemaCode(zeCollection, schemaCode);
		try {
			schemasDisplayManager.getSchema(zeCollection, schemaCode);
			fail("MetadataSchemasRuntimeException.NoSuchSchema expected");
		} catch (MetadataSchemasRuntimeException.NoSuchSchema e) {
			//OK
		}
	}
}
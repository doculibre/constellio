package com.constellio.app.services.migrations.scripts;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.records.wrappers.ImportExportAudit;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

import static java.util.Arrays.asList;

public class CoreMigrationTo_7_4_2 implements MigrationScript {

	@Override
	public String getVersion() {
		return "7.4.2";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory)
			throws Exception {
		new CoreSchemaAlterationFor7_4_2(collection, migrationResourcesProvider, appLayerFactory).migrate();

		SchemasDisplayManager metadataSchemasDisplayManager = appLayerFactory.getMetadataSchemasDisplayManager();
		metadataSchemasDisplayManager.saveSchema(metadataSchemasDisplayManager.getSchema(collection, ImportExportAudit.DEFAULT_SCHEMA)
				.withTableMetadataCodes(asList(
						ImportExportAudit.DEFAULT_SCHEMA + "_" + Schemas.CREATED_BY.getLocalCode(),
						ImportExportAudit.DEFAULT_SCHEMA + "_" + ImportExportAudit.START_DATE,
						ImportExportAudit.DEFAULT_SCHEMA + "_" + ImportExportAudit.END_DATE,
						ImportExportAudit.DEFAULT_SCHEMA + "_" + ImportExportAudit.ERRORS
				)));
	}

	private class CoreSchemaAlterationFor7_4_2 extends MetadataSchemasAlterationHelper {
		public CoreSchemaAlterationFor7_4_2(String collection, MigrationResourcesProvider migrationResourcesProvider,
				AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			MetadataSchemaBuilder importExportAudit = typesBuilder.createNewSchemaType(ImportExportAudit.SCHEMA_TYPE).getDefaultSchema();

			importExportAudit.createUndeletable(ImportExportAudit.START_DATE).setType(MetadataValueType.DATE_TIME).setSystemReserved(true);
			importExportAudit.createUndeletable(ImportExportAudit.END_DATE).setType(MetadataValueType.DATE_TIME).setSystemReserved(true);
			importExportAudit.createUndeletable(ImportExportAudit.ERRORS).setType(MetadataValueType.STRING).setMultivalue(true).setSystemReserved(true);
			importExportAudit.createUndeletable(ImportExportAudit.TYPE).defineAsEnum(ImportExportAudit.ExportImport.class).setSystemReserved(true);
		}
	}
}
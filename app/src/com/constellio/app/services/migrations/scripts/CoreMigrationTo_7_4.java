package com.constellio.app.services.migrations.scripts;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.wrappers.ImportExportAudit;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import org.camunda.bpm.model.bpmn.instance.Import;

public class CoreMigrationTo_7_4 implements MigrationScript {

	@Override
	public String getVersion() {
		return "7.4";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory)
			throws Exception {
		new CoreSchemaAlterationFor7_4(collection, migrationResourcesProvider, appLayerFactory).migrate();
	}

	private class CoreSchemaAlterationFor7_4 extends MetadataSchemasAlterationHelper {
		public CoreSchemaAlterationFor7_4(String collection, MigrationResourcesProvider migrationResourcesProvider,
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
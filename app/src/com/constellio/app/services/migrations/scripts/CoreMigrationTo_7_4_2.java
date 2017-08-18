package com.constellio.app.services.migrations.scripts;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.model.calculators.TemporaryRecordDestructionDateCalculator;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.records.wrappers.ExportAudit;
import com.constellio.model.entities.records.wrappers.ImportAudit;
import com.constellio.model.entities.records.wrappers.TemporaryRecord;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
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
		metadataSchemasDisplayManager.saveSchema(metadataSchemasDisplayManager.getSchema(collection, TemporaryRecord.DEFAULT_SCHEMA)
				.withTableMetadataCodes(asList(
						TemporaryRecord.DEFAULT_SCHEMA + "_" + Schemas.CREATED_BY.getLocalCode(),
						TemporaryRecord.DEFAULT_SCHEMA + "_" + Schemas.CREATED_ON.getLocalCode(),
						TemporaryRecord.DEFAULT_SCHEMA + "_" + TemporaryRecord.DESTRUCTION_DATE,
						TemporaryRecord.DEFAULT_SCHEMA + "_" + TemporaryRecord.CONTENT
				)));
	}

	private class CoreSchemaAlterationFor7_4_2 extends MetadataSchemasAlterationHelper {
		public CoreSchemaAlterationFor7_4_2(String collection, MigrationResourcesProvider migrationResourcesProvider,
				AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			migrateTemporaryRecord(typesBuilder);
		}

		private void migrateTemporaryRecord(MetadataSchemaTypesBuilder typesBuilder) {
			MetadataSchemaTypeBuilder schemaType = typesBuilder.createNewSchemaType(TemporaryRecord.SCHEMA_TYPE);
			MetadataSchemaBuilder defaultSchema = schemaType.getDefaultSchema();

			defaultSchema.createUndeletable(TemporaryRecord.DESTRUCTION_DATE).setType(MetadataValueType.DATE_TIME).defineDataEntry().asCalculated(TemporaryRecordDestructionDateCalculator.class).setSystemReserved(true);
			defaultSchema.createUndeletable(TemporaryRecord.CONTENT).setType(MetadataValueType.CONTENT).setSystemReserved(true);

			MetadataSchemaBuilder importAuditSchema = schemaType.createCustomSchema(ImportAudit.SCHEMA);
			importAuditSchema.createUndeletable(ImportAudit.ERRORS).setType(MetadataValueType.STRING).setMultivalue(true).setSystemReserved(true);
			importAuditSchema.createUndeletable(ImportAudit.END_DATE).setType(MetadataValueType.DATE_TIME).setSystemReserved(true);

			MetadataSchemaBuilder exportAuditSchema = schemaType.createCustomSchema(ExportAudit.SCHEMA);
			exportAuditSchema.createUndeletable(ImportAudit.END_DATE).setType(MetadataValueType.DATE_TIME).setSystemReserved(true);
		}
	}
}
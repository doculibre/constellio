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
import com.constellio.model.services.schemas.validators.TemporaryRecordValidator;

import static java.util.Arrays.asList;

public class CoreMigrationTo_7_5 implements MigrationScript {
	@Override
	public String getVersion() {
		return "7.5";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory) throws Exception {
		new CoreSchemaAlterationFor_7_5(collection, migrationResourcesProvider, appLayerFactory).migrate();
		SchemasDisplayManager metadataSchemasDisplayManager = appLayerFactory.getMetadataSchemasDisplayManager();
		metadataSchemasDisplayManager.saveSchema(metadataSchemasDisplayManager.getSchema(collection, TemporaryRecord.DEFAULT_SCHEMA)
				.withTableMetadataCodes(asList(
						TemporaryRecord.DEFAULT_SCHEMA + "_" + Schemas.CREATED_BY.getLocalCode(),
						TemporaryRecord.DEFAULT_SCHEMA + "_" + Schemas.CREATED_ON.getLocalCode(),
						TemporaryRecord.DEFAULT_SCHEMA + "_" + TemporaryRecord.DESTRUCTION_DATE,
						TemporaryRecord.DEFAULT_SCHEMA + "_" + TemporaryRecord.CONTENT
				)));
	}

	class CoreSchemaAlterationFor_7_5 extends MetadataSchemasAlterationHelper {

		protected CoreSchemaAlterationFor_7_5(String collection, MigrationResourcesProvider migrationResourcesProvider,
											  AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			MetadataSchemaTypeBuilder schemaType = typesBuilder.createNewSchemaTypeWithSecurity(TemporaryRecord.SCHEMA_TYPE);
			MetadataSchemaBuilder defaultSchema = schemaType.getDefaultSchema();

			defaultSchema.createUndeletable(TemporaryRecord.DESTRUCTION_DATE).setType(MetadataValueType.DATE_TIME).defineDataEntry().asCalculated(TemporaryRecordDestructionDateCalculator.class).setSystemReserved(true);
			defaultSchema.createUndeletable(TemporaryRecord.CONTENT).setType(MetadataValueType.CONTENT).setSystemReserved(true);
			defaultSchema.createUndeletable(TemporaryRecord.DAY_BEFORE_DESTRUCTION).setType(MetadataValueType.NUMBER).setDefaultValue(7).addValidator(TemporaryRecordValidator.class);

			MetadataSchemaBuilder importAuditSchema = schemaType.createCustomSchema(ImportAudit.SCHEMA);
			importAuditSchema.createUndeletable(ImportAudit.ERRORS).setType(MetadataValueType.STRING).setMultivalue(true).setSystemReserved(true);
			importAuditSchema.createUndeletable(ImportAudit.END_DATE).setType(MetadataValueType.DATE_TIME).setSystemReserved(true);

			MetadataSchemaBuilder exportAuditSchema = schemaType.createCustomSchema(ExportAudit.SCHEMA);
			exportAuditSchema.createUndeletable(ImportAudit.END_DATE).setType(MetadataValueType.DATE_TIME).setSystemReserved(true);
		}
	}
}

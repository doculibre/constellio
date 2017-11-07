package com.constellio.app.services.migrations.scripts;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.wrappers.ExportAudit;
import com.constellio.model.entities.records.wrappers.ImportAudit;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class CoreMigrationTo_7_6_2_1 implements MigrationScript {
	@Override
	public String getVersion() {
		return "7.6.2.1";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory)
			throws Exception {
		new CoreSchemaAlterationFor_7_6_2_1_deleteStep(collection, migrationResourcesProvider, appLayerFactory).migrate();
		new CoreSchemaAlterationFor_7_6_2_1(collection, migrationResourcesProvider, appLayerFactory).migrate();
	}

	class CoreSchemaAlterationFor_7_6_2_1_deleteStep extends MetadataSchemasAlterationHelper {

		protected CoreSchemaAlterationFor_7_6_2_1_deleteStep(String collection,
				MigrationResourcesProvider migrationResourcesProvider,
				AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			MetadataSchemaBuilder importAuditSchema = typesBuilder.getSchema(ImportAudit.FULL_SCHEMA);
			importAuditSchema.deleteMetadataWithoutValidation(ImportAudit.ERRORS);
		}
	}

	class CoreSchemaAlterationFor_7_6_2_1 extends MetadataSchemasAlterationHelper {

		protected CoreSchemaAlterationFor_7_6_2_1(String collection, MigrationResourcesProvider migrationResourcesProvider,
				AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			MetadataSchemaBuilder importAuditSchema = typesBuilder.getSchema(ImportAudit.FULL_SCHEMA)
					.addLabel(Language.French, "Audits d'importation");
			importAuditSchema.createUndeletable(ImportAudit.ERRORS).setType(MetadataValueType.TEXT).setSystemReserved(true);

			MetadataSchemaBuilder exportAuditSchema = typesBuilder.getSchema(ExportAudit.FULL_SCHEMA)
					.addLabel(Language.French, "Audits d'exportation");

		}
	}
}

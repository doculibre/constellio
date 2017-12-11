package com.constellio.app.services.migrations.scripts;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.batchprocess.BatchProcess;
import com.constellio.model.entities.records.wrappers.BatchProcessReport;
import com.constellio.model.entities.records.wrappers.ExportAudit;
import com.constellio.model.entities.records.wrappers.ImportAudit;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class CoreMigrationTo_7_7 implements MigrationScript {
	@Override
	public String getVersion() {
		return "7.7";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory)
			throws Exception {
		new CoreSchemaAlterationFor_7_7(collection, migrationResourcesProvider, appLayerFactory).migrate();
	}

	class CoreSchemaAlterationFor_7_7 extends MetadataSchemasAlterationHelper {

		protected CoreSchemaAlterationFor_7_7(String collection,
				MigrationResourcesProvider migrationResourcesProvider,
				AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			MetadataSchemaTypeBuilder temporaryRecord = typesBuilder.getSchemaType(BatchProcessReport.SCHEMA_TYPE);
			MetadataSchemaBuilder batchProcessReportSchema = temporaryRecord.createCustomSchema(BatchProcessReport.SCHEMA);
			batchProcessReportSchema.createUndeletable(BatchProcessReport.ERRORS).setType(MetadataValueType.TEXT).setSystemReserved(true);
			batchProcessReportSchema.createUndeletable(BatchProcessReport.MESSAGES).setType(MetadataValueType.STRING).setMultivalue(true).setSystemReserved(true);
			batchProcessReportSchema.createUndeletable(BatchProcessReport.LINKED_BATCH_PROCESS).setType(MetadataValueType.STRING).setUniqueValue(true).setSystemReserved(true);
		}
	}
}

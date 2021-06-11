package com.constellio.app.services.migrations.scripts;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.dao.services.records.DataStore;
import com.constellio.model.entities.records.wrappers.BatchProcessReport;
import com.constellio.model.entities.records.wrappers.RecordAuthorization;
import com.constellio.model.entities.records.wrappers.ScriptReport;
import com.constellio.model.entities.records.wrappers.SearchEvent;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

import static com.constellio.model.entities.records.wrappers.RecordAuthorization.OVERRIDE_INHERITED;
import static com.constellio.model.entities.schemas.MetadataValueType.BOOLEAN;
import static com.constellio.model.entities.schemas.MetadataValueType.NUMBER;
import static com.constellio.model.entities.schemas.MetadataValueType.STRING;

public class CoreMigrationTo_7_7_1 implements MigrationScript {
	@Override
	public String getVersion() {
		return "7.7.1";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory)
			throws Exception {
		new CoreSchemaAlterationFor_7_7_1(collection, migrationResourcesProvider, appLayerFactory).migrate();
	}

	class CoreSchemaAlterationFor_7_7_1 extends MetadataSchemasAlterationHelper {

		protected CoreSchemaAlterationFor_7_7_1(String collection,
												MigrationResourcesProvider migrationResourcesProvider,
												AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			MetadataSchemaTypeBuilder temporaryRecord = typesBuilder.getSchemaType(BatchProcessReport.SCHEMA_TYPE);
			if (!temporaryRecord.hasSchema(BatchProcessReport.SCHEMA)) {
				MetadataSchemaBuilder batchProcessReportSchema = temporaryRecord.createCustomSchema(BatchProcessReport.SCHEMA);
				batchProcessReportSchema.createUndeletable(BatchProcessReport.ERRORS).setType(MetadataValueType.TEXT)
						.setSystemReserved(true);
				batchProcessReportSchema.createUndeletable(BatchProcessReport.SKIPPED_RECORDS).setType(MetadataValueType.STRING)
						.setMultivalue(true).setSystemReserved(true);
				batchProcessReportSchema.createUndeletable(BatchProcessReport.LINKED_BATCH_PROCESS)
						.setType(MetadataValueType.STRING).setUniqueValue(true).setSystemReserved(true);
			}

			MetadataSchemaBuilder authorizationSchema = typesBuilder.getSchema(RecordAuthorization.DEFAULT_SCHEMA);
			if (!authorizationSchema.hasMetadata(OVERRIDE_INHERITED)) {
				authorizationSchema.createUndeletable(OVERRIDE_INHERITED).setType(BOOLEAN);
			}

			if (!temporaryRecord.hasSchema(ScriptReport.SCHEMA)) {
				temporaryRecord.createCustomSchema(ScriptReport.SCHEMA);
			}

			if (!typesBuilder.hasSchemaType(SearchEvent.SCHEMA_TYPE)) {
				MetadataSchemaTypeBuilder searchEvent = typesBuilder.createNewSchemaTypeWithSecurity(SearchEvent.SCHEMA_TYPE);
				searchEvent.setDataStore(DataStore.EVENTS);
				searchEvent.createMetadata(SearchEvent.USERNAME).setType(STRING);
				searchEvent.createMetadata(SearchEvent.QUERY).setType(STRING);
				searchEvent.createMetadata(SearchEvent.PAGE_NAVIGATION_COUNT).setType(NUMBER);
				searchEvent.createMetadata(SearchEvent.CLICK_COUNT).setType(NUMBER);
				searchEvent.createMetadata(SearchEvent.PARAMS).setType(STRING).setMultivalue(true);
				searchEvent.createMetadata(SearchEvent.ORIGINAL_QUERY).setType(STRING);
				searchEvent.createMetadata(SearchEvent.NUM_FOUND).setType(NUMBER);
				searchEvent.createMetadata(SearchEvent.Q_TIME).setType(NUMBER);
				searchEvent.createMetadata(SearchEvent.DWELL_TIME).setType(NUMBER).setDefaultValue(0);
			}
		}
	}
}

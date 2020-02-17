package com.constellio.app.services.migrations.scripts;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.wrappers.Event;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class CoreMigrationTo_9_0_1_40 implements MigrationScript {

	@Override
	public String getVersion() {
		return "9.0.1.40";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory) throws Exception {
		new CoreMigrationTo_9_0_1_40.SchemaAlterationFor9_0_1_40(collection, migrationResourcesProvider, appLayerFactory).migrate();
	}

	class SchemaAlterationFor9_0_1_40 extends MetadataSchemasAlterationHelper {

		protected SchemaAlterationFor9_0_1_40(String collection, MigrationResourcesProvider migrationResourcesProvider,
											  AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			MetadataSchemaBuilder eventSchema = typesBuilder.getDefaultSchema(Event.SCHEMA_TYPE);
			eventSchema.createUndeletable(Event.BATCH_PROCESS_ID).setType(MetadataValueType.TEXT);
			eventSchema.createUndeletable(Event.TOTAL_MODIFIED_RECORD).setType(MetadataValueType.NUMBER);
			eventSchema.createUndeletable(Event.CONTENT).setType(MetadataValueType.CONTENT);
		}
	}
}

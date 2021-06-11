package com.constellio.app.services.migrations.scripts;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.wrappers.Event;
import com.constellio.model.entities.records.wrappers.RecordAuthorization;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

import static com.constellio.model.entities.schemas.MetadataValueType.STRING;

public class CoreMigrationTo_9_0_1_4 implements MigrationScript {


	@Override
	public String getVersion() {
		return "9.0.1.4";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory) throws Exception {
		new CoreMigrationTo_9_0_1_4.SchemaAlterationFor9_0_1_4(collection, migrationResourcesProvider, appLayerFactory).migrate();
	}

	class SchemaAlterationFor9_0_1_4 extends MetadataSchemasAlterationHelper {

		protected SchemaAlterationFor9_0_1_4(String collection, MigrationResourcesProvider migrationResourcesProvider,
											 AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {

			typesBuilder.getSchema(RecordAuthorization.DEFAULT_SCHEMA).create(RecordAuthorization.SHARED_BY)
					.setType(STRING).setCacheIndex(true);
			typesBuilder.getSchema(Event.DEFAULT_SCHEMA).create(Event.SHARED_BY)
					.setType(STRING);
		}

	}
}

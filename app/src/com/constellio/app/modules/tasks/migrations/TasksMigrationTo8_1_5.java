package com.constellio.app.modules.tasks.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

import static com.constellio.model.entities.schemas.MetadataValueType.BOOLEAN;

public class TasksMigrationTo8_1_5 extends MigrationHelper implements MigrationScript {

	@Override
	public String getVersion() {
		return "8.1.5";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory)
			throws Exception {
		new SchemaAlterationFor8_1_5(collection, migrationResourcesProvider, appLayerFactory).migrate();
	}

	class SchemaAlterationFor8_1_5 extends MetadataSchemasAlterationHelper {

		protected SchemaAlterationFor8_1_5(String collection, MigrationResourcesProvider migrationResourcesProvider,
										   AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			typesBuilder.getDefaultSchema(User.SCHEMA_TYPE).createUndeletable(User.ASSIGNATION_EMAIL_RECEPTION_DISABLED)
					.setType(BOOLEAN).setSystemReserved(true);
		}
	}
}

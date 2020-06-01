package com.constellio.app.modules.tasks.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.tasks.model.wrappers.TaskUser;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class TasksMigrationTo9_1_0_2 implements MigrationScript {
	@Override
	public String getVersion() {
		return "9.1.0.2";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory) throws Exception {
		new SchemaAlterationFor9_1_0_2(collection, migrationResourcesProvider, appLayerFactory).migrate();
	}

	private class SchemaAlterationFor9_1_0_2 extends MetadataSchemasAlterationHelper {
		SchemaAlterationFor9_1_0_2(String collection, MigrationResourcesProvider migrationResourcesProvider,
								   AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			MetadataSchemaBuilder task = typesBuilder.getSchemaType(TaskUser.SCHEMA_TYPE).getDefaultSchema();
			task.createUndeletable(TaskUser.DELEGATION_TASK_USER).setType(MetadataValueType.REFERENCE)
					.defineReferencesTo(typesBuilder.getDefaultSchema(User.SCHEMA_TYPE));
		}
	}
}

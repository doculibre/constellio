package com.constellio.app.modules.tasks.migrations;

import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.model.wrappers.types.TaskStatus;
import com.constellio.app.modules.tasks.services.TasksSchemasRecordsServices;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class TasksMigrationTo7_2 implements MigrationScript {
	@Override
	public String getVersion() {
		return "7.2";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory)
			throws Exception {

		TasksSchemasRecordsServices tasks = new TasksSchemasRecordsServices(collection, appLayerFactory);
		final TaskStatus status = tasks.getTaskStatusWithCode(TaskStatus.STANDBY_CODE);

		appLayerFactory.getModelLayerFactory().getMetadataSchemasManager()
				.modify(collection, new MetadataSchemaTypesAlteration() {
					@Override
					public void alter(MetadataSchemaTypesBuilder types) {
						for (MetadataSchemaBuilder schema : types.getSchemaType(Task.SCHEMA_TYPE).getAllSchemas()) {
							schema.getMetadata(Task.STATUS).setDefaultValue(status.getId());
						}
					}
				});

	}
}

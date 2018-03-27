package com.constellio.app.modules.tasks.migrations;

import java.util.ArrayList;

import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.entities.schemasDisplay.SchemaDisplayConfig;
import com.constellio.app.modules.tasks.TaskTypes;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.model.wrappers.types.TaskStatus;
import com.constellio.app.modules.tasks.services.TasksSchemasRecordsServices;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemaDisplayManagerTransaction;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.schemas.MetadataSchema;
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

		configureTableMetadatas(collection, appLayerFactory);
	}

	private void configureTableMetadatas(String collection, AppLayerFactory factory) {
		SchemaDisplayManagerTransaction transaction = new SchemaDisplayManagerTransaction();
		SchemasDisplayManager manager = factory.getMetadataSchemasDisplayManager();

		for (MetadataSchema metadataSchema : TaskTypes.taskSchemas(factory, collection)) {
			if ("default".equals(metadataSchema.getLocalCode())) {
				SchemaDisplayConfig config = manager.getSchema(collection, metadataSchema.getCode());
				transaction.add(config.withTableMetadataCodes(config.getSearchResultsMetadataCodes()));
			} else {
				SchemaDisplayConfig customConfig = manager.getSchema(collection, metadataSchema.getCode());
				transaction.add(customConfig.withTableMetadataCodes(new ArrayList<String>()));
			}
		}
		manager.execute(transaction);
	}
}

package com.constellio.app.modules.tasks.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemaTypesDisplayTransactionBuilder;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class TasksMigrationTo7_5 implements MigrationScript {
	@Override
	public String getVersion() {
		return "7.5";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory)
			throws Exception {
		new TaskSchemaAlterationFor7_5(collection, migrationResourcesProvider, appLayerFactory).migrate();
		SchemasDisplayManager displayManager = appLayerFactory.getMetadataSchemasDisplayManager();
		SchemaTypesDisplayTransactionBuilder transactionBuilder = displayManager.newTransactionBuilderFor(collection);

		transactionBuilder.add(displayManager.getSchema(collection, Task.DEFAULT_SCHEMA)
				.withNewTableMetadatas(Task.DEFAULT_SCHEMA + "_" + Task.STARRED_BY_USERS));
		displayManager.execute(transactionBuilder.build());
	}

	private class TaskSchemaAlterationFor7_5 extends MetadataSchemasAlterationHelper {

		public TaskSchemaAlterationFor7_5(String collection, MigrationResourcesProvider migrationResourcesProvider,
				AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			MetadataSchemaBuilder taskSchema = typesBuilder.getDefaultSchema(Task.SCHEMA_TYPE);
			taskSchema.createUndeletable(Task.STARRED_BY_USERS).setSystemReserved(true)
					.setType(MetadataValueType.STRING).setMultivalue(true);
		}
	}
}

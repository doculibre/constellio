package com.constellio.app.services.migrations.scripts;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemaTypesDisplayTransactionBuilder;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class CoreMigrationTo_8_3_1_1 implements MigrationScript {

	@Override
	public String getVersion() {
		return "8.3.1.1";
	}


	@Override
	public void migrate(final String collection, MigrationResourcesProvider provider,
						AppLayerFactory appLayerFactory)
			throws Exception {

		new CoreSchemaAlterationFor8_3_1_1(collection, provider, appLayerFactory);

		updateTaskFormAndDisplay(collection, appLayerFactory);
	}

	private void updateTaskFormAndDisplay(String collection, AppLayerFactory appLayerFactory) {
		if (!Collection.SYSTEM_COLLECTION.equals(collection)) {
			SchemasDisplayManager manager = appLayerFactory.getMetadataSchemasDisplayManager();

			SchemaTypesDisplayTransactionBuilder transactionBuilder = manager.newTransactionBuilderFor(collection);

			transactionBuilder.in(Task.SCHEMA_TYPE)
					.removeFromDisplay(Task.READ_BY_USER);

			transactionBuilder.in(Task.SCHEMA_TYPE)
					.removeFromForm(Task.READ_BY_USER);

			manager.execute(transactionBuilder.build());
		}
	}

	private class CoreSchemaAlterationFor8_3_1_1 extends MetadataSchemasAlterationHelper {
		public CoreSchemaAlterationFor8_3_1_1(String collection,
											  MigrationResourcesProvider migrationResourcesProvider,
											  AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder builder) {

		}
	}
}

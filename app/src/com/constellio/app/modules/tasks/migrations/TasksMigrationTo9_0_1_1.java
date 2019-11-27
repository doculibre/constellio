package com.constellio.app.modules.tasks.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.tasks.model.calculators.TaskVisibleInTreesCalculator;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

import static com.constellio.model.entities.schemas.MetadataValueType.BOOLEAN;

public class TasksMigrationTo9_0_1_1 extends MigrationHelper implements MigrationScript {

	@Override
	public String getVersion() {
		return "9.0.1.1";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory)
			throws Exception {
		new TasksMigrationTo9_0_1_1.SchemaAlterationFor9_0_1_1(collection, migrationResourcesProvider, appLayerFactory).migrate();
	}

	class SchemaAlterationFor9_0_1_1 extends MetadataSchemasAlterationHelper {

		protected SchemaAlterationFor9_0_1_1(String collection, MigrationResourcesProvider migrationResourcesProvider,
											 AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			MetadataSchemaBuilder task = typesBuilder.getSchema(Task.DEFAULT_SCHEMA);
			task.get(Schemas.VISIBLE_IN_TREES).defineDataEntry().asCalculated(TaskVisibleInTreesCalculator.class);

			if (!task.hasMetadata(User.ASSIGNATION_EMAIL_RECEPTION_DISABLED)) {
				typesBuilder.getDefaultSchema(User.SCHEMA_TYPE).createUndeletable(User.ASSIGNATION_EMAIL_RECEPTION_DISABLED)
						.setType(BOOLEAN).setSystemReserved(true);
			}
		}
	}
}

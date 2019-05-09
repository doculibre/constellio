package com.constellio.app.modules.tasks.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.tasks.model.calculators.TaskHiddenStatusCalculator;
import com.constellio.app.modules.tasks.model.calculators.TaskIsLateCalculator;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemaTypesDisplayTransactionBuilder;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class TasksMigrationTo8_2_42 extends MigrationHelper implements MigrationScript {

	@Override
	public String getVersion() {
		return "8.2.42";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory)
			throws Exception {
		new SchemaAlterationFor8_2_42(collection, migrationResourcesProvider, appLayerFactory).migrate();

		SchemasDisplayManager manager = appLayerFactory.getMetadataSchemasDisplayManager();
		SchemaTypesDisplayTransactionBuilder transaction = manager.newTransactionBuilderFor(collection);

		transaction.in(Task.SCHEMA_TYPE).addToDisplay(Task.IS_LATE).atTheEnd();

		manager.execute(transaction.build());

	}

	class SchemaAlterationFor8_2_42 extends MetadataSchemasAlterationHelper {

		protected SchemaAlterationFor8_2_42(String collection, MigrationResourcesProvider migrationResourcesProvider,
											AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			MetadataSchemaBuilder task = typesBuilder.getSchemaType(Task.SCHEMA_TYPE).getDefaultSchema();
			task.createUndeletable(Task.IS_LATE).setType(MetadataValueType.BOOLEAN)
					.defineDataEntry().asCalculated(TaskIsLateCalculator.class);
			task.createUndeletable(Task.WORK_HOURS).setType(MetadataValueType.NUMBER);
			task.createUndeletable(Task.ESTIMATED_HOURS).setType(MetadataValueType.NUMBER);
			task.get(Schemas.HIDDEN.getLocalCode()).defineDataEntry().asCalculated(TaskHiddenStatusCalculator.class);
			if (task.hasMetadata("workflowTaskSort")) {
				task.get("workflowTaskSort").setMarkedForDeletion(true);
			}
		}
	}
}

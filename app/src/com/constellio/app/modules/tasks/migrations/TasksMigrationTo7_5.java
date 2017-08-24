package com.constellio.app.modules.tasks.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.services.ValueListItemSchemaTypeBuilder;
import com.constellio.app.modules.rm.wrappers.structures.CommentFactory;
import com.constellio.app.modules.tasks.model.calculators.TaskFollowersCalculator;
import com.constellio.app.modules.tasks.model.calculators.TaskNextReminderOnCalculator;
import com.constellio.app.modules.tasks.model.calculators.TaskTokensCalculator;
import com.constellio.app.modules.tasks.model.validators.TaskValidator;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.model.wrappers.structures.TaskFollowerFactory;
import com.constellio.app.modules.tasks.model.wrappers.structures.TaskReminderFactory;
import com.constellio.app.modules.tasks.model.wrappers.types.TaskStatus;
import com.constellio.app.modules.tasks.model.wrappers.types.TaskType;
import com.constellio.app.modules.tasks.services.TasksSchemasRecordsServices;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemaTypesDisplayTransactionBuilder;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.schemas.validators.PercentageValidator;

import java.util.Map;

import static com.constellio.app.modules.rm.services.ValueListItemSchemaTypeBuilder.ValueListItemSchemaTypeBuilderOptions.codeMetadataRequiredAndUnique;
import static com.constellio.model.entities.schemas.MetadataValueType.*;

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
		SchemaTypesDisplayTransactionBuilder transaction = displayManager.newTransactionBuilderFor(collection);

		transaction.add(displayManager.getSchema(collection, Task.DEFAULT_SCHEMA)
				.withNewTableMetadatas(Task.DEFAULT_SCHEMA + "_" + Task.STARRED_BY_USERS));
		displayManager.execute(transaction.build());
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

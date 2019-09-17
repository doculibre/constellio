package com.constellio.app.modules.tasks.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.entities.schemasDisplay.SchemaDisplayConfig;
import com.constellio.app.modules.tasks.model.calculators.TaskCollaboratorsTokensCalculator;
import com.constellio.app.modules.tasks.model.validators.TaskValidator;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemaDisplayManagerTransaction;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

import static com.constellio.app.entities.schemasDisplay.enums.MetadataInputType.LOOKUP;
import static com.constellio.model.entities.schemas.MetadataValueType.BOOLEAN;
import static com.constellio.model.entities.schemas.MetadataValueType.REFERENCE;

public class TasksMigrationTo9_0 extends MigrationHelper implements MigrationScript {

	@Override
	public String getVersion() {
		return "9.0";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory)
			throws Exception {
		new SchemaAlterationFor9_0(collection, migrationResourcesProvider, appLayerFactory).migrate();
		setupDisplay(collection, appLayerFactory);
	}

	class SchemaAlterationFor9_0 extends MetadataSchemasAlterationHelper {

		protected SchemaAlterationFor9_0(String collection, MigrationResourcesProvider migrationResourcesProvider,
										 AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			MetadataSchemaTypeBuilder schemaType = types().getSchemaType(Task.SCHEMA_TYPE);
			MetadataSchemaBuilder defaultSchema = schemaType.getDefaultSchema();
			defaultSchema.defineValidators().add(TaskValidator.class);

			defaultSchema.createUndeletable(Task.TASK_COLLABORATORS).setType(REFERENCE).setMultivalue(true)
					.defineReferencesTo(typesBuilder.getSchemaType(User.SCHEMA_TYPE));
			defaultSchema.createUndeletable(Task.TASK_COLLABORATORS_WRITE_AUTHORIZATIONS).setType(BOOLEAN).setMultivalue(true);
			defaultSchema.createUndeletable(Task.TASK_COLLABORATORS_GROUPS).setType(REFERENCE).setMultivalue(true)
					.defineReferencesTo(typesBuilder.getSchemaType(Group.SCHEMA_TYPE));
			defaultSchema.createUndeletable(Task.TASK_COLLABORATORS_GROUPS_WRITE_AUTHORIZATIONS).setType(BOOLEAN).setMultivalue(true);
			defaultSchema.get(Schemas.TOKENS.getLocalCode()).defineDataEntry().asCalculated(TaskCollaboratorsTokensCalculator.class);
		}
	}

	private void setupDisplay(String collection, AppLayerFactory appLayerFactory) {
		SchemasDisplayManager manager = appLayerFactory.getMetadataSchemasDisplayManager();
		SchemaDisplayManagerTransaction transaction = new SchemaDisplayManagerTransaction();
		String assignmentTab = "init.userTask.assignment";
		SchemaDisplayConfig tasksDisplayConfig = manager.getSchema(collection, Task.DEFAULT_SCHEMA);
		transaction.add(manager.getMetadata(collection, Task.DEFAULT_SCHEMA, Task.TASK_COLLABORATORS)
				.withMetadataGroup(assignmentTab).withInputType(LOOKUP).withVisibleInAdvancedSearchStatus(true));
		transaction.add(manager.getMetadata(collection, Task.DEFAULT_SCHEMA, Task.TASK_COLLABORATORS_WRITE_AUTHORIZATIONS)
				.withMetadataGroup(assignmentTab).withInputType(LOOKUP).withVisibleInAdvancedSearchStatus(true));
		transaction.add(tasksDisplayConfig
				.withNewFormAndDisplayMetadatas(Task.DEFAULT_SCHEMA + "_" + Task.TASK_COLLABORATORS, Task.DEFAULT_SCHEMA + "_" + Task.TASK_COLLABORATORS_GROUPS));
		transaction.add(manager.getMetadata(collection, Task.DEFAULT_SCHEMA, Task.TASK_COLLABORATORS_GROUPS)
				.withMetadataGroup(assignmentTab).withInputType(LOOKUP).withVisibleInAdvancedSearchStatus(true));
		transaction.add(manager.getMetadata(collection, Task.DEFAULT_SCHEMA, Task.TASK_COLLABORATORS_GROUPS_WRITE_AUTHORIZATIONS)
				.withMetadataGroup(assignmentTab).withInputType(LOOKUP).withVisibleInAdvancedSearchStatus(true));
		manager.execute(transaction);
	}
}

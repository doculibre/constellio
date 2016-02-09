package com.constellio.app.modules.tasks.migrations;

import static com.constellio.model.entities.schemas.MetadataValueType.STRING;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.tasks.model.calculators.DecisionsTasksCalculator;
import com.constellio.app.modules.tasks.model.calculators.WorkflowTaskSortCalculator;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.model.wrappers.TaskStatusType;
import com.constellio.app.modules.tasks.model.wrappers.Workflow;
import com.constellio.app.modules.tasks.model.wrappers.WorkflowInstance;
import com.constellio.app.modules.tasks.model.wrappers.WorkflowInstanceStatus;
import com.constellio.app.modules.tasks.model.wrappers.types.TaskStatus;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemaTypesDisplayTransactionBuilder;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.structures.MapStringListStringStructureFactory;
import com.constellio.model.entities.structures.MapStringStringStructureFactory;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class TasksMigrationTo6_0 implements MigrationScript {
	@Override
	public String getVersion() {
		return "6.0";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory)
			throws Exception {
		new SchemaAlterationsFor6_0(collection, migrationResourcesProvider, appLayerFactory).migrate();
		configureDisplayConfig(collection, appLayerFactory);
	}

	private void configureDisplayConfig(String collection, AppLayerFactory appLayerFactory) {
		SchemasDisplayManager manager = appLayerFactory.getMetadataSchemasDisplayManager();
		SchemaTypesDisplayTransactionBuilder transactionBuilder = manager.newTransactionBuilderFor(collection)
				.in(Task.SCHEMA_TYPE)
				.addToForm(Task.DECISION, Task.RELATIVE_DUE_DATE)
				.atTheEnd()
				.in(Task.SCHEMA_TYPE)
				.addToDisplay(Task.DECISION, Task.WORKFLOW, Task.WORKFLOW_INSTANCE, Task.RELATIVE_DUE_DATE)
				.atTheEnd();
		manager.execute(transactionBuilder.build());
	}

	private class SchemaAlterationsFor6_0 extends MetadataSchemasAlterationHelper {
		public SchemaAlterationsFor6_0(String collection, MigrationResourcesProvider migrationResourcesProvider,
				AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			MetadataSchemaTypeBuilder userSchemaType = typesBuilder.getSchemaType(User.SCHEMA_TYPE);

			MetadataSchemaTypeBuilder workflowSchemaType = typesBuilder.createNewSchemaType(Workflow.SCHEMA_TYPE);
			workflowSchemaType.setSecurity(false);
			MetadataSchemaBuilder workflowSchema = workflowSchemaType.getDefaultSchema();
			workflowSchema.create(Workflow.CODE).setType(STRING).setUniqueValue(true);

			MetadataSchemaTypeBuilder workflowInstanceSchemaType = typesBuilder.createNewSchemaType(WorkflowInstance.SCHEMA_TYPE);
			workflowInstanceSchemaType.setSecurity(false);
			MetadataSchemaBuilder workflowInstanceSchema = workflowInstanceSchemaType.getDefaultSchema();
			workflowInstanceSchema.create(WorkflowInstance.STARTED_BY).defineReferencesTo(userSchemaType);
			workflowInstanceSchema.create(WorkflowInstance.STARTED_ON).setType(MetadataValueType.DATE_TIME);
			workflowInstanceSchema.create(WorkflowInstance.STATUS).defineAsEnum(WorkflowInstanceStatus.class);
			workflowInstanceSchema.create(WorkflowInstance.WORKFLOW).defineReferencesTo(workflowSchemaType);
			workflowInstanceSchema.create(WorkflowInstance.EXTRA_FIELDS)
					.defineStructureFactory(MapStringListStringStructureFactory.class);

			MetadataSchemaTypeBuilder taskSchemaType = typesBuilder.getSchemaType(Task.SCHEMA_TYPE);
			MetadataSchemaBuilder taskSchema = taskSchemaType.getDefaultSchema();
			taskSchema.create(Task.STATUS_TYPE).defineAsEnum(TaskStatusType.class).defineDataEntry().asCopied(
					taskSchema.getMetadata(Task.STATUS),
					typesBuilder.getDefaultSchema(TaskStatus.SCHEMA_TYPE).getMetadata(TaskStatus.STATUS_TYPE)
			);

			taskSchema.create(Task.WORKFLOW).defineReferencesTo(workflowSchemaType);
			taskSchema.create(Task.MODEL_TASK).defineReferencesTo(taskSchemaType);
			taskSchema.create(Task.DECISION).setType(MetadataValueType.STRING);
			taskSchema.create(Task.WORKFLOW_INSTANCE).defineReferencesTo(workflowInstanceSchemaType);
			taskSchema.create(Task.IS_MODEL).setType(MetadataValueType.BOOLEAN);
			taskSchema.create(Task.NEXT_TASK_CREATED).setType(MetadataValueType.BOOLEAN);
			taskSchema.create(Task.WORKFLOW_TASK_SORT).setType(MetadataValueType.NUMBER)
					.defineDataEntry().asCalculated(WorkflowTaskSortCalculator.class);
			taskSchema.create(Task.NEXT_TASKS).setType(MetadataValueType.REFERENCE).setMultivalue(true)
					.defineReferencesTo(taskSchemaType)
					.defineDataEntry().asCalculated(DecisionsTasksCalculator.class);
			taskSchema.create(Task.NEXT_TASKS_DECISIONS).defineStructureFactory(MapStringStringStructureFactory.class);
			taskSchema.create(Task.RELATIVE_DUE_DATE).setType(MetadataValueType.NUMBER);
		}
	}
}

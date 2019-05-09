package com.constellio.app.modules.tasks.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.tasks.TasksPermissionsTo;
import com.constellio.app.modules.tasks.model.calculators.DecisionsTasksCalculator;
import com.constellio.app.modules.tasks.model.wrappers.BetaWorkflow;
import com.constellio.app.modules.tasks.model.wrappers.BetaWorkflowInstance;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.model.wrappers.TaskStatusType;
import com.constellio.app.modules.tasks.model.wrappers.WorkflowInstanceStatus;
import com.constellio.app.modules.tasks.model.wrappers.types.TaskStatus;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.migrations.CoreRoles;
import com.constellio.app.services.schemasDisplay.SchemaTypesDisplayTransactionBuilder;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.security.Role;
import com.constellio.model.entities.structures.MapStringListStringStructureFactory;
import com.constellio.model.entities.structures.MapStringStringStructureFactory;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.security.roles.RolesManager;
import com.constellio.model.services.security.roles.RolesManagerRuntimeException.RolesManagerRuntimeException_Validation;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.model.entities.schemas.MetadataValueType.STRING;

public class TasksMigrationTo6_0 implements MigrationScript {
	@Override
	public String getVersion() {
		return "6.0";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory)
			throws Exception {
		new SchemaAlterationsFor6_0(collection, migrationResourcesProvider, appLayerFactory).migrate();
		configureDisplayConfig(collection, appLayerFactory);
		updatePermissions(collection, appLayerFactory);
	}

	private void configureDisplayConfig(String collection, AppLayerFactory appLayerFactory) {
		SchemasDisplayManager manager = appLayerFactory.getMetadataSchemasDisplayManager();
		SchemaTypesDisplayTransactionBuilder transactionBuilder = manager.newTransactionBuilderFor(collection)
				.in(Task.SCHEMA_TYPE)
				.addToForm(Task.DECISION, Task.RELATIVE_DUE_DATE)
				.atTheEnd()
				.in(Task.SCHEMA_TYPE)
				.addToDisplay(Task.DECISION, Task.BETA_WORKFLOW, Task.BETA_WORKFLOW_INSTANCE, Task.RELATIVE_DUE_DATE)
				.atTheEnd();
		manager.execute(transactionBuilder.build());
	}

	private void updatePermissions(String collection, AppLayerFactory factory) {
		RolesManager roleManager = factory.getModelLayerFactory().getRolesManager();

		Role administrator;

		try {
			administrator = roleManager.getRole(collection, CoreRoles.ADMINISTRATOR);
		} catch (RolesManagerRuntimeException_Validation e) {
			administrator = roleManager.addRole(
					new Role(collection, CoreRoles.ADMINISTRATOR, CoreRoles.ADMINISTRATOR, CorePermissions.PERMISSIONS.getAll()));
		}
		List<String> permissions = new ArrayList<>(administrator.getOperationPermissions());
		permissions.add(TasksPermissionsTo.MANAGE_WORKFLOWS);

		roleManager.updateRole(administrator.withPermissions(permissions));
	}

	private class SchemaAlterationsFor6_0 extends MetadataSchemasAlterationHelper {
		public SchemaAlterationsFor6_0(String collection, MigrationResourcesProvider migrationResourcesProvider,
									   AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			MetadataSchemaTypeBuilder userSchemaType = typesBuilder.getSchemaType(User.SCHEMA_TYPE);

			MetadataSchemaTypeBuilder workflowSchemaType = typesBuilder.createNewSchemaType(BetaWorkflow.SCHEMA_TYPE);
			workflowSchemaType.setSecurity(false);
			MetadataSchemaBuilder workflowSchema = workflowSchemaType.getDefaultSchema();
			workflowSchema.create(BetaWorkflow.CODE).setType(STRING).setUniqueValue(true);

			MetadataSchemaTypeBuilder workflowInstanceSchemaType = typesBuilder.createNewSchemaType(BetaWorkflowInstance.SCHEMA_TYPE);
			workflowInstanceSchemaType.setSecurity(false);
			MetadataSchemaBuilder workflowInstanceSchema = workflowInstanceSchemaType.getDefaultSchema();
			workflowInstanceSchema.create(BetaWorkflowInstance.STARTED_BY).defineReferencesTo(userSchemaType);
			workflowInstanceSchema.create(BetaWorkflowInstance.STARTED_ON).setType(MetadataValueType.DATE_TIME);
			workflowInstanceSchema.create(BetaWorkflowInstance.STATUS).defineAsEnum(WorkflowInstanceStatus.class);
			workflowInstanceSchema.create(BetaWorkflowInstance.WORKFLOW).defineReferencesTo(workflowSchemaType);
			workflowInstanceSchema.create(BetaWorkflowInstance.EXTRA_FIELDS)
					.defineStructureFactory(MapStringListStringStructureFactory.class);

			MetadataSchemaTypeBuilder taskSchemaType = typesBuilder.getSchemaType(Task.SCHEMA_TYPE);
			MetadataSchemaBuilder taskSchema = taskSchemaType.getDefaultSchema();
			taskSchema.create(Task.STATUS_TYPE).defineAsEnum(TaskStatusType.class).defineDataEntry().asCopied(
					taskSchema.getMetadata(Task.STATUS),
					typesBuilder.getDefaultSchema(TaskStatus.SCHEMA_TYPE).getMetadata(TaskStatus.STATUS_TYPE)
			);

			taskSchema.create(Task.BETA_WORKFLOW).defineReferencesTo(workflowSchemaType);
			taskSchema.create(Task.MODEL_TASK).defineReferencesTo(taskSchemaType);
			taskSchema.create(Task.DECISION).setType(MetadataValueType.STRING);
			taskSchema.create(Task.BETA_WORKFLOW_INSTANCE).defineReferencesTo(workflowInstanceSchemaType);
			taskSchema.create(Task.IS_MODEL).setType(MetadataValueType.BOOLEAN);
			taskSchema.create(Task.BETA_NEXT_TASK_CREATED).setType(MetadataValueType.BOOLEAN);
			taskSchema.create(Task.BETA_NEXT_TASKS).setType(MetadataValueType.REFERENCE).setMultivalue(true)
					.defineReferencesTo(taskSchemaType)
					.defineDataEntry().asCalculated(DecisionsTasksCalculator.class);
			taskSchema.create(Task.BETA_NEXT_TASKS_DECISIONS).defineStructureFactory(MapStringStringStructureFactory.class);
			taskSchema.create(Task.RELATIVE_DUE_DATE).setType(MetadataValueType.NUMBER);

		}
	}
}

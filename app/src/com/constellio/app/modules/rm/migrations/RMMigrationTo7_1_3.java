package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.constants.RMRoles;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.services.TasksSchemasRecordsServices;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.migrations.CoreRoles;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.Event;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

import static java.util.Arrays.asList;

public class RMMigrationTo7_1_3 extends MigrationHelper implements MigrationScript {
	@Override
	public String getVersion() {
		return "7.1.3";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory)
			throws Exception {
		new SchemaAlterationFor7_1_3(collection, migrationResourcesProvider, appLayerFactory).migrate();
		TasksSchemasRecordsServices taskSchemas = new TasksSchemasRecordsServices(collection, appLayerFactory);
		Transaction transaction = new Transaction();
		transaction.add(taskSchemas.newTaskType().setCode("borrowRequest").setTitle("Demande d'emprunt").setLinkedSchema("userTask_borrowRequest"));
		transaction.add(taskSchemas.newTaskType().setCode("returnRequest").setTitle("Demande de retour").setLinkedSchema("userTask_returnRequest"));
		transaction.add(taskSchemas.newTaskType().setCode("reactivationRequest").setTitle("Demande de réactivation").setLinkedSchema("userTask_reactivationRequest"));
		transaction.add(taskSchemas.newTaskType().setCode("borrowExtensionRequest").setTitle("Demande de prolongation d'emprunt").setLinkedSchema("userTask_borrowExtensionRequest"));
		appLayerFactory.getModelLayerFactory().newRecordServices().execute(transaction);
		migrateRoles(collection, appLayerFactory.getModelLayerFactory());
	}

	private void migrateRoles(String collection, ModelLayerFactory modelLayerFactory) {
		Role rgd = modelLayerFactory.getRolesManager().getRole(collection, RMRoles.RGD);
		Role admin = modelLayerFactory.getRolesManager().getRole(collection, CoreRoles.ADMINISTRATOR);
		Role manager = modelLayerFactory.getRolesManager().getRole(collection, RMRoles.MANAGER);
		Role user = modelLayerFactory.getRolesManager().getRole(collection, RMRoles.USER);
		modelLayerFactory.getRolesManager().updateRole(rgd.withNewPermissions(asList(
				RMPermissionsTo.MANAGE_REQUEST_ON_DOCUMENT,
				RMPermissionsTo.BORROWING_REQUEST_ON_DOCUMENT,
				RMPermissionsTo.MANAGE_REQUEST_ON_FOLDER,
				RMPermissionsTo.BORROWING_REQUEST_ON_FOLDER
		)));
		modelLayerFactory.getRolesManager().updateRole(admin.withNewPermissions(asList(
				RMPermissionsTo.MANAGE_REQUEST_ON_DOCUMENT,
				RMPermissionsTo.BORROWING_REQUEST_ON_DOCUMENT,
				RMPermissionsTo.MANAGE_REQUEST_ON_FOLDER,
				RMPermissionsTo.BORROWING_REQUEST_ON_FOLDER
		)));

		modelLayerFactory.getRolesManager().updateRole(manager.withNewPermissions(asList(
				RMPermissionsTo.BORROWING_REQUEST_ON_DOCUMENT,
				RMPermissionsTo.BORROWING_REQUEST_ON_FOLDER
		)));

		modelLayerFactory.getRolesManager().updateRole(user.withNewPermissions(asList(
				RMPermissionsTo.BORROWING_REQUEST_ON_DOCUMENT,
				RMPermissionsTo.BORROWING_REQUEST_ON_FOLDER
		)));
	}

	private class SchemaAlterationFor7_1_3 extends MetadataSchemasAlterationHelper {

		protected SchemaAlterationFor7_1_3(String collection, MigrationResourcesProvider migrationResourcesProvider,
				AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			MetadataSchemaTypeBuilder taskSchemaType = typesBuilder.getSchemaType(Task.SCHEMA_TYPE);
			taskSchemaType.createCustomSchema("borrowRequest");
			taskSchemaType.createCustomSchema("returnRequest");
			taskSchemaType.createCustomSchema("reactivationRequest");
			taskSchemaType.createCustomSchema("borrowExtensionRequest");

			typesBuilder.getDefaultSchema(ContainerRecord.SCHEMA_TYPE).createUndeletable(ContainerRecord.BORROW_RETURN_DATE)
					.setType(MetadataValueType.DATE_TIME);

			MetadataSchemaTypeBuilder eventSchemaType = typesBuilder.getSchemaType(Event.SCHEMA_TYPE);
			eventSchemaType.getDefaultSchema().create(Event.RECEIVER_NAME).setType(MetadataValueType.STRING).defineDataEntry().asManual();
			eventSchemaType.getDefaultSchema().create(Event.TASK).setType(MetadataValueType.STRING).defineDataEntry().asManual();
			eventSchemaType.getDefaultSchema().create(Event.DESCRIPTION).setType(MetadataValueType.TEXT).defineDataEntry().asManual();
		}
	}
}

package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.constants.RMRoles;
import com.constellio.app.modules.rm.RMEmailTemplateConstants;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.model.wrappers.request.BorrowRequest;
import com.constellio.app.modules.tasks.model.wrappers.request.ExtensionRequest;
import com.constellio.app.modules.tasks.model.wrappers.request.ReactivationRequest;
import com.constellio.app.modules.tasks.model.wrappers.request.ReturnRequest;
import com.constellio.app.modules.tasks.services.TasksSchemasRecordsServices;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.migrations.CoreRoles;
import com.constellio.data.dao.managers.config.ConfigManagerException;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.Event;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

import static java.util.Arrays.asList;

public class RMMigrationTo7_1_3 extends MigrationHelper implements MigrationScript {

    private AppLayerFactory appLayerFactory;
    private String collection;
    private MigrationResourcesProvider migrationResourcesProvider;

    @Override
    public String getVersion() {
        return "7.1.3";
    }

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory)
			throws Exception {
		this.appLayerFactory = appLayerFactory;
		this.collection = collection;
		this.migrationResourcesProvider = migrationResourcesProvider;
		new SchemaAlterationFor7_1_3(collection, migrationResourcesProvider, appLayerFactory).migrate();
		TasksSchemasRecordsServices taskSchemas = new TasksSchemasRecordsServices(collection, appLayerFactory);
		Transaction transaction = new Transaction();
		transaction.add(taskSchemas.newTaskType().setCode("borrowRequest").setTitle("Demande d'emprunt").setLinkedSchema(Task.SCHEMA_TYPE + "_" + BorrowRequest.SCHEMA_NAME));
		transaction.add(taskSchemas.newTaskType().setCode("returnRequest").setTitle("Demande de retour").setLinkedSchema(Task.SCHEMA_TYPE + "_" + ReturnRequest.SCHEMA_NAME));
		transaction.add(taskSchemas.newTaskType().setCode("reactivationRequest").setTitle("Demande de réactivation").setLinkedSchema(Task.SCHEMA_TYPE + "_" + ReactivationRequest.SCHEMA_NAME));
		transaction.add(taskSchemas.newTaskType().setCode("borrowExtensionRequest").setTitle("Demande de prolongation d'emprunt").setLinkedSchema(Task.SCHEMA_TYPE + "_" + ExtensionRequest.SCHEMA_NAME));
		appLayerFactory.getModelLayerFactory().newRecordServices().execute(transaction);
		migrateRoles(collection, appLayerFactory.getModelLayerFactory());
		reloadEmailTemplates();
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

	private void reloadEmailTemplates() {
		if (appLayerFactory.getModelLayerFactory().getCollectionsListManager().getCollectionLanguages(collection).get(0).equals("en")) {
			reloadEmailTemplate("alertBorrowedTemplate_en.html", RMEmailTemplateConstants.ALERT_BORROWED);
			reloadEmailTemplate("alertReturnedTemplate_en.html", RMEmailTemplateConstants.ALERT_RETURNED);
			reloadEmailTemplate("alertReactivatedTemplate_en.html", RMEmailTemplateConstants.ALERT_REACTIVATED);
			reloadEmailTemplate("alertBorrowingExtendedTemplate_en.html", RMEmailTemplateConstants.ALERT_BORROWING_EXTENTED);
		} else {
			reloadEmailTemplate("alertBorrowedTemplate.html", RMEmailTemplateConstants.ALERT_BORROWED);
			reloadEmailTemplate("alertReturnedTemplate.html", RMEmailTemplateConstants.ALERT_RETURNED);
			reloadEmailTemplate("alertReactivatedTemplate.html", RMEmailTemplateConstants.ALERT_REACTIVATED);
			reloadEmailTemplate("alertBorrowingExtendedTemplate.html", RMEmailTemplateConstants.ALERT_BORROWING_EXTENTED);
		}
	}

	private void reloadEmailTemplate(final String templateFileName, final String templateId) {
		final InputStream templateInputStream = migrationResourcesProvider.getStream(templateFileName);

		try {
			appLayerFactory.getModelLayerFactory().getEmailTemplatesManager().replaceCollectionTemplate(templateId, collection, templateInputStream);
		} catch (IOException | ConfigManagerException.OptimisticLockingConfiguration e) {
			throw new RuntimeException(e);
		} finally {
			IOUtils.closeQuietly(templateInputStream);
		}
	}

	private class SchemaAlterationFor7_1_3 extends MetadataSchemasAlterationHelper {

		protected SchemaAlterationFor7_1_3(String collection, MigrationResourcesProvider migrationResourcesProvider,
				AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			MetadataSchemaTypeBuilder taskSchemaType = typesBuilder.getSchemaType(Task.SCHEMA_TYPE);
			taskSchemaType.createCustomSchema(BorrowRequest.SCHEMA_NAME);
			taskSchemaType.createCustomSchema(ReturnRequest.SCHEMA_NAME);
			taskSchemaType.createCustomSchema(ReactivationRequest.SCHEMA_NAME);
			taskSchemaType.createCustomSchema(ExtensionRequest.SCHEMA_NAME);

			typesBuilder.getDefaultSchema(ContainerRecord.SCHEMA_TYPE).createUndeletable(ContainerRecord.BORROW_RETURN_DATE)
					.setType(MetadataValueType.DATE_TIME);

			typesBuilder.getSchema(Task.SCHEMA_TYPE + "_" + ExtensionRequest.SCHEMA_NAME).create(ExtensionRequest.EXTENSION_VALUE).defineDataEntry().asManual().setType(MetadataValueType.DATE);
			typesBuilder.getSchema(Task.SCHEMA_TYPE + "_" + ExtensionRequest.SCHEMA_NAME).create(ExtensionRequest.ACCEPTED).defineDataEntry().asManual().setType(MetadataValueType.BOOLEAN).setDefaultValue(false);
			typesBuilder.getSchema(Task.SCHEMA_TYPE + "_" + BorrowRequest.SCHEMA_NAME).create(BorrowRequest.ACCEPTED).defineDataEntry().asManual().setType(MetadataValueType.BOOLEAN).setDefaultValue(false);
			typesBuilder.getSchema(Task.SCHEMA_TYPE + "_" + ReactivationRequest.SCHEMA_NAME).create(ReactivationRequest.ACCEPTED).defineDataEntry().asManual().setType(MetadataValueType.BOOLEAN).setDefaultValue(false);
			typesBuilder.getSchema(Task.SCHEMA_TYPE + "_" + ReturnRequest.SCHEMA_NAME).create(ReturnRequest.ACCEPTED).defineDataEntry().asManual().setType(MetadataValueType.BOOLEAN).setDefaultValue(false);

			MetadataSchemaTypeBuilder eventSchemaType = typesBuilder.getSchemaType(Event.SCHEMA_TYPE);
			eventSchemaType.getDefaultSchema().create(Event.RECEIVER_NAME).setType(MetadataValueType.STRING).defineDataEntry().asManual();
			eventSchemaType.getDefaultSchema().create(Event.TASK).setType(MetadataValueType.STRING).defineDataEntry().asManual();
			eventSchemaType.getDefaultSchema().create(Event.DESCRIPTION).setType(MetadataValueType.TEXT).defineDataEntry().asManual();
		}
	}
}

package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.entities.schemasDisplay.enums.MetadataInputType;
import com.constellio.app.modules.rm.RMEmailTemplateConstants;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.constants.RMRoles;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.RMTask;
import com.constellio.app.modules.rm.wrappers.RMTaskType;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.model.wrappers.request.BorrowRequest;
import com.constellio.app.modules.tasks.model.wrappers.request.ExtensionRequest;
import com.constellio.app.modules.tasks.model.wrappers.request.ReactivationRequest;
import com.constellio.app.modules.tasks.model.wrappers.request.ReturnRequest;
import com.constellio.app.modules.tasks.services.TasksSchemasRecordsServices;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.migrations.CoreRoles;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
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

import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
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
		transaction.add(taskSchemas.newTaskType().setCode(RMTaskType.BORROW_REQUEST).setTitle("Demande d'emprunt")
				.setLinkedSchema(Task.SCHEMA_TYPE + "_" + BorrowRequest.SCHEMA_NAME));
		transaction.add(taskSchemas.newTaskType().setCode(RMTaskType.RETURN_REQUEST).setTitle("Demande de retour")
				.setLinkedSchema(Task.SCHEMA_TYPE + "_" + ReturnRequest.SCHEMA_NAME));
		transaction.add(taskSchemas.newTaskType().setCode(RMTaskType.REACTIVATION_REQUEST).setTitle("Demande de réactivation")
				.setLinkedSchema(Task.SCHEMA_TYPE + "_" + ReactivationRequest.SCHEMA_NAME));
		transaction.add(taskSchemas.newTaskType().setCode(RMTaskType.BORROW_EXTENSION_REQUEST).setTitle("Demande de prolongation d'emprunt")
				.setLinkedSchema(Task.SCHEMA_TYPE + "_" + ExtensionRequest.SCHEMA_NAME));
		appLayerFactory.getModelLayerFactory().newRecordServices().execute(transaction);
		adjustSchemaDisplay();
		migrateRoles(collection, appLayerFactory.getModelLayerFactory());
		reloadEmailTemplates();
	}

	private void adjustSchemaDisplay() {
		SchemasDisplayManager displayManager = appLayerFactory.getMetadataSchemasDisplayManager();
		displayManager.saveSchema(displayManager.getSchema(collection, Task.DEFAULT_SCHEMA)
				.withNewFormMetadata(Task.DEFAULT_SCHEMA + "_" + Task.LINKED_CONTAINERS));
		displayManager.saveSchema(displayManager.getSchema(collection, Task.DEFAULT_SCHEMA)
				.withNewFormMetadata(Task.DEFAULT_SCHEMA + "_" + Task.REASON));
		displayManager.saveMetadata(displayManager.getMetadata(collection, Task.DEFAULT_SCHEMA + "_" + Task.REASON)
				.withInputType(MetadataInputType.TEXTAREA));

		String detailsTab = migrationResourcesProvider.getDefaultLanguageString("init.userTask.details");
		displayManager.saveMetadata(displayManager.getMetadata(collection, RMTask.DEFAULT_SCHEMA, RMTask.LINKED_CONTAINERS)
				.withMetadataGroup(detailsTab));
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
				RMPermissionsTo.BORROWING_REQUEST_ON_DOCUMENT,
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

			typesBuilder.getSchema(Task.DEFAULT_SCHEMA).createUndeletable(Task.LINKED_CONTAINERS).setType(MetadataValueType.REFERENCE)
					.defineReferencesTo(typesBuilder.getDefaultSchema(ContainerRecord.SCHEMA_TYPE)).setMultivalue(true);
			typesBuilder.getSchema(Task.DEFAULT_SCHEMA).createUndeletable(Task.COMPLETED_BY).setType(MetadataValueType.REFERENCE)
					.defineReferencesTo(typesBuilder.getDefaultSchema(User.SCHEMA_TYPE)).setSystemReserved(true);
			typesBuilder.getSchema(Task.DEFAULT_SCHEMA).createUndeletable(Task.REASON).setType(MetadataValueType.TEXT);
			typesBuilder.getSchema(ExtensionRequest.FULL_SCHEMA_NAME).createUndeletable(ExtensionRequest.EXTENSION_VALUE)
					.setType(MetadataValueType.DATE);
			typesBuilder.getSchema(ExtensionRequest.FULL_SCHEMA_NAME).createUndeletable(ExtensionRequest.ACCEPTED)
					.setType(MetadataValueType.BOOLEAN).setDefaultValue(null);
			typesBuilder.getSchema(BorrowRequest.FULL_SCHEMA_NAME).createUndeletable(BorrowRequest.ACCEPTED)
					.setType(MetadataValueType.BOOLEAN).setDefaultValue(null);
			typesBuilder.getSchema(ReactivationRequest.FULL_SCHEMA_NAME).createUndeletable(ReactivationRequest.ACCEPTED)
					.setType(MetadataValueType.BOOLEAN).setDefaultValue(null);
			typesBuilder.getSchema(ReturnRequest.FULL_SCHEMA_NAME).createUndeletable(ReturnRequest.ACCEPTED)
					.setType(MetadataValueType.BOOLEAN).setDefaultValue(null);
			typesBuilder.getSchema(ExtensionRequest.FULL_SCHEMA_NAME).createUndeletable(ExtensionRequest.APPLICANT)
					.setType(MetadataValueType.REFERENCE).defineReferencesTo(typesBuilder.getDefaultSchema(User.SCHEMA_TYPE))
					.setSystemReserved(true);
			typesBuilder.getSchema(BorrowRequest.FULL_SCHEMA_NAME).createUndeletable(BorrowRequest.APPLICANT)
					.setType(MetadataValueType.REFERENCE).defineReferencesTo(typesBuilder.getDefaultSchema(User.SCHEMA_TYPE))
					.setSystemReserved(true);
			typesBuilder.getSchema(ReactivationRequest.FULL_SCHEMA_NAME).createUndeletable(ReactivationRequest.APPLICANT)
					.setType(MetadataValueType.REFERENCE).defineReferencesTo(typesBuilder.getDefaultSchema(User.SCHEMA_TYPE))
					.setSystemReserved(true);
			typesBuilder.getSchema(ReturnRequest.FULL_SCHEMA_NAME).createUndeletable(ReturnRequest.APPLICANT)
					.setType(MetadataValueType.REFERENCE).defineReferencesTo(typesBuilder.getDefaultSchema(User.SCHEMA_TYPE))
					.setSystemReserved(true);


			MetadataSchemaTypeBuilder eventSchemaType = typesBuilder.getSchemaType(Event.SCHEMA_TYPE);
			eventSchemaType.getDefaultSchema().create(Event.RECEIVER_NAME).setType(STRING).defineDataEntry().asManual();
			eventSchemaType.getDefaultSchema().create(Event.TASK).setType(STRING).defineDataEntry().asManual();
			eventSchemaType.getDefaultSchema().create(Event.DESCRIPTION).setType(MetadataValueType.TEXT).defineDataEntry().asManual();
		}
	}
}

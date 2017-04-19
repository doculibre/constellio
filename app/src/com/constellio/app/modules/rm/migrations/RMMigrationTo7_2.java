package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.entities.schemasDisplay.enums.MetadataInputType;
import com.constellio.app.modules.rm.RMEmailTemplateConstants;
import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.modules.rm.constants.RMRoles;
import com.constellio.app.modules.rm.model.calculators.FolderDecommissioningDateCalculator2;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.*;
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
import com.constellio.data.utils.BatchBuilderIterator;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.RecordUpdateOptions;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.Event;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import org.apache.chemistry.opencmis.commons.impl.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.constellio.model.entities.schemas.MetadataValueType.REFERENCE;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;

/**
 * Created by Charles Blanchette on 2017-03-22.
 */
public class RMMigrationTo7_2 implements MigrationScript {
    @Override
    public String getVersion() {
        return "7.2";
    }

    @Override
    public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
                        AppLayerFactory appLayerFactory) {
        RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
        RecordServices recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
        List<Category> categories = rm.wrapCategorys(appLayerFactory.getModelLayerFactory().newSearchServices().search(
                new LogicalSearchQuery().setCondition(from(rm.category.schemaType()).returnAll())));
        Map<String, String> descriptions = new HashMap<>();
        for(Category category: categories) {
            descriptions.put(category.getId(), category.getDescription());
            category.setDescription(null);
        }

        BatchBuilderIterator<Category> batchIterator = new BatchBuilderIterator<>(categories.iterator(), 1000);

        while (batchIterator.hasNext()) {
            Transaction transaction = new Transaction();
            transaction.addAll(batchIterator.next());
            transaction.setOptions(RecordUpdateOptions.validationExceptionSafeOptions());
            try {
                recordServices.execute(transaction);
            } catch (RecordServicesException e) {
                e.printStackTrace();
                throw new RuntimeException("Failed to set categories descriptions to null in RMMigration7_2");
            }
        }

        new SchemaAlterationFor7_2_step1(collection, migrationResourcesProvider, appLayerFactory).migrate();
        new SchemaAlterationFor7_2_step2(collection, migrationResourcesProvider, appLayerFactory).migrate();
        SchemasDisplayManager displayManager = appLayerFactory.getMetadataSchemasDisplayManager();
        displayManager.saveMetadata(displayManager.getMetadata(collection, Category.DEFAULT_SCHEMA + "_" + Category.DESCRIPTION).withInputType(MetadataInputType.RICHTEXT));

        categories = rm.wrapCategorys(appLayerFactory.getModelLayerFactory().newSearchServices().search(
                new LogicalSearchQuery().setCondition(from(rm.category.schemaType()).returnAll())));
        batchIterator = new BatchBuilderIterator<>(categories.iterator(), 1000);
        for(Category category: categories) {
            category.setDescription(descriptions.get(category.getId()));
        }

        while (batchIterator.hasNext()) {
            Transaction transaction = new Transaction();
            transaction.addAll(batchIterator.next());
            transaction.setOptions(RecordUpdateOptions.validationExceptionSafeOptions());
            try {
                recordServices.execute(transaction);
            } catch (RecordServicesException e) {
                e.printStackTrace();
                throw new RuntimeException("Failed to migrate categories descriptions in RMMigration7_2");
            }
        }

        migrateSearchableSchemaTypes(collection, migrationResourcesProvider, appLayerFactory);
        updateNewPermissions(appLayerFactory, collection);
		TasksSchemasRecordsServices taskSchemas = new TasksSchemasRecordsServices(collection, appLayerFactory);
		try {
			createNewTaskTypes(appLayerFactory, taskSchemas);
		} catch (RecordServicesException e) {
			throw new RuntimeException("Failed to create new task types in RMMigration7_2");
		}
		adjustSchemaDisplay(appLayerFactory, migrationResourcesProvider, collection);
		migrateRoles(collection, appLayerFactory.getModelLayerFactory());
		reloadEmailTemplates(appLayerFactory, migrationResourcesProvider, collection);
    }

    private void updateNewPermissions(AppLayerFactory appLayerFactory, String collection) {
        ModelLayerFactory modelLayerFactory = appLayerFactory.getModelLayerFactory();

        Role admRole = modelLayerFactory.getRolesManager().getRole(collection, RMRoles.RGD);
        modelLayerFactory.getRolesManager().updateRole(admRole.withNewPermissions(asList(CorePermissions.MANAGE_SEARCH_BOOST)));
    }

    private void migrateSearchableSchemaTypes(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory) {
        SchemasDisplayManager manager = appLayerFactory.getMetadataSchemasDisplayManager();
        manager.saveType(manager.getType(collection, ContainerRecord.SCHEMA_TYPE).withSimpleAndAdvancedSearchStatus(true));
        manager.saveType(manager.getType(collection, StorageSpace.SCHEMA_TYPE).withSimpleAndAdvancedSearchStatus(true));

        manager.saveMetadata(manager.getMetadata(collection, StorageSpace.DEFAULT_SCHEMA + "_" + StorageSpace.NUMBER_OF_CONTAINERS).withVisibleInAdvancedSearchStatus(true));
    }

	private void createNewTaskTypes(AppLayerFactory appLayerFactory, TasksSchemasRecordsServices taskSchemas) throws RecordServicesException {
		Transaction transaction = new Transaction();
		transaction.setOptions(RecordUpdateOptions.validationExceptionSafeOptions());
		transaction.add(taskSchemas.newTaskType().setCode(RMTaskType.BORROW_REQUEST).setTitle("Demande d'emprunt")
				.setLinkedSchema(Task.SCHEMA_TYPE + "_" + BorrowRequest.SCHEMA_NAME));
		transaction.add(taskSchemas.newTaskType().setCode(RMTaskType.RETURN_REQUEST).setTitle("Demande de retour")
				.setLinkedSchema(Task.SCHEMA_TYPE + "_" + ReturnRequest.SCHEMA_NAME));
		transaction.add(taskSchemas.newTaskType().setCode(RMTaskType.REACTIVATION_REQUEST).setTitle("Demande de réactivation")
				.setLinkedSchema(Task.SCHEMA_TYPE + "_" + ReactivationRequest.SCHEMA_NAME));
		transaction.add(taskSchemas.newTaskType().setCode(RMTaskType.BORROW_EXTENSION_REQUEST)
				.setTitle("Demande de prolongation d'emprunt")
				.setLinkedSchema(Task.SCHEMA_TYPE + "_" + ExtensionRequest.SCHEMA_NAME));
		appLayerFactory.getModelLayerFactory().newRecordServices().execute(transaction);
	}

	private void adjustSchemaDisplay(AppLayerFactory appLayerFactory, MigrationResourcesProvider migrationResourcesProvider, String collection) {
		SchemasDisplayManager displayManager = appLayerFactory.getMetadataSchemasDisplayManager();

		displayManager.saveSchema(displayManager.getSchema(collection, Folder.DEFAULT_SCHEMA)
				.withNewDisplayMetadataBefore(Folder.DEFAULT_SCHEMA + "_" + Folder.REACTIVATION_DATES,
						Folder.DEFAULT_SCHEMA + "_" + Folder.COMMENTS)
				.withNewDisplayMetadataBefore(Folder.DEFAULT_SCHEMA + "_" + Folder.REACTIVATION_USERS,
						Folder.DEFAULT_SCHEMA + "_" + Folder.COMMENTS)
				.withNewDisplayMetadataBefore(Folder.DEFAULT_SCHEMA + "_" + Folder.PREVIOUS_TRANSFER_DATES,
						Folder.DEFAULT_SCHEMA + "_" + Folder.COMMENTS)
				.withNewDisplayMetadataBefore(Folder.DEFAULT_SCHEMA + "_" + Folder.PREVIOUS_DEPOSIT_DATES,
						Folder.DEFAULT_SCHEMA + "_" + Folder.COMMENTS));

		displayManager.saveSchema(displayManager.getSchema(collection, Task.DEFAULT_SCHEMA)
				.withNewFormMetadata(Task.DEFAULT_SCHEMA + "_" + Task.LINKED_CONTAINERS));
		displayManager.saveSchema(displayManager.getSchema(collection, Task.DEFAULT_SCHEMA)
				.withNewFormMetadata(Task.DEFAULT_SCHEMA + "_" + Task.REASON));
		displayManager.saveMetadata(displayManager.getMetadata(collection, Task.DEFAULT_SCHEMA + "_" + Task.REASON)
				.withInputType(MetadataInputType.TEXTAREA));

		String detailsTab = migrationResourcesProvider.getDefaultLanguageString("init.userTask.details");
		displayManager.saveMetadata(displayManager.getMetadata(collection, RMTask.DEFAULT_SCHEMA, RMTask.LINKED_CONTAINERS)
				.withMetadataGroup(detailsTab));

		displayManager.saveSchema(displayManager.getSchema(collection, Event.DEFAULT_SCHEMA)
				.withNewTableMetadatas(Event.DEFAULT_SCHEMA + "_" + Event.RECEIVER_NAME,
						Event.DEFAULT_SCHEMA + "_" + Event.TASK,
						Event.DEFAULT_SCHEMA + "_" + Event.DESCRIPTION));
	}

	private void migrateRoles(String collection, ModelLayerFactory modelLayerFactory) {
		Role rgd = modelLayerFactory.getRolesManager().getRole(collection, RMRoles.RGD);
		Role admin = modelLayerFactory.getRolesManager().getRole(collection, CoreRoles.ADMINISTRATOR);
		Role manager = modelLayerFactory.getRolesManager().getRole(collection, RMRoles.MANAGER);
		Role user = modelLayerFactory.getRolesManager().getRole(collection, RMRoles.USER);
		modelLayerFactory.getRolesManager().updateRole(rgd.withNewPermissions(asList(
				RMPermissionsTo.MANAGE_REQUEST_ON_CONTAINER,
				RMPermissionsTo.BORROWING_FOLDER_DIRECTLY,
				RMPermissionsTo.BORROWING_REQUEST_ON_CONTAINER,
				RMPermissionsTo.MANAGE_REQUEST_ON_FOLDER,
				RMPermissionsTo.BORROWING_REQUEST_ON_FOLDER,
				RMPermissionsTo.REACTIVATION_REQUEST_ON_FOLDER,
				RMPermissionsTo.BORROW_CONTAINER
		)));
		modelLayerFactory.getRolesManager().updateRole(admin.withNewPermissions(asList(
				RMPermissionsTo.BORROWING_REQUEST_ON_CONTAINER,
				RMPermissionsTo.BORROWING_REQUEST_ON_FOLDER,
				RMPermissionsTo.REACTIVATION_REQUEST_ON_FOLDER,
				RMPermissionsTo.BORROW_CONTAINER
		)));

		modelLayerFactory.getRolesManager().updateRole(manager.withNewPermissions(asList(
				RMPermissionsTo.BORROWING_REQUEST_ON_CONTAINER,
				RMPermissionsTo.BORROWING_REQUEST_ON_FOLDER,
				RMPermissionsTo.REACTIVATION_REQUEST_ON_FOLDER,
				RMPermissionsTo.BORROW_CONTAINER
		)));

		modelLayerFactory.getRolesManager().updateRole(user.withNewPermissions(asList(
				RMPermissionsTo.BORROWING_REQUEST_ON_CONTAINER,
				RMPermissionsTo.BORROWING_REQUEST_ON_FOLDER,
				RMPermissionsTo.REACTIVATION_REQUEST_ON_FOLDER,
				RMPermissionsTo.BORROW_CONTAINER
		)));
	}

	private void reloadEmailTemplates(AppLayerFactory appLayerFactory, MigrationResourcesProvider migrationResourcesProvider, String collection) {
		if (appLayerFactory.getModelLayerFactory().getCollectionsListManager().getCollectionLanguages(collection).get(0)
				.equals("en")) {
			reloadEmailTemplate("alertBorrowedTemplate_en.html", RMEmailTemplateConstants.ALERT_BORROWED_ACCEPTED, appLayerFactory, migrationResourcesProvider, collection);
			reloadEmailTemplate("alertReturnedTemplate_en.html", RMEmailTemplateConstants.ALERT_RETURNED_ACCEPTED, appLayerFactory, migrationResourcesProvider, collection);
			reloadEmailTemplate("alertReactivatedTemplate_en.html", RMEmailTemplateConstants.ALERT_REACTIVATED_ACCEPTED, appLayerFactory, migrationResourcesProvider, collection);
			reloadEmailTemplate("alertBorrowingExtendedTemplate_en.html", RMEmailTemplateConstants.ALERT_BORROWING_EXTENTED_ACCEPTED, appLayerFactory, migrationResourcesProvider, collection);
			reloadEmailTemplate("alertBorrowedTemplateDenied_en.html", RMEmailTemplateConstants.ALERT_BORROWED_DENIED, appLayerFactory, migrationResourcesProvider, collection);
			reloadEmailTemplate("alertReturnedTemplateDenied_en.html", RMEmailTemplateConstants.ALERT_RETURNED_DENIED, appLayerFactory, migrationResourcesProvider, collection);
			reloadEmailTemplate("alertReactivatedTemplateDenied_en.html", RMEmailTemplateConstants.ALERT_REACTIVATED_DENIED, appLayerFactory, migrationResourcesProvider, collection);
			reloadEmailTemplate("alertBorrowingExtendedTemplateDenied_en.html", RMEmailTemplateConstants.ALERT_BORROWING_EXTENTED_DENIED, appLayerFactory, migrationResourcesProvider, collection);
		} else {
			reloadEmailTemplate("alertBorrowedTemplate.html", RMEmailTemplateConstants.ALERT_BORROWED_ACCEPTED, appLayerFactory, migrationResourcesProvider, collection);
			reloadEmailTemplate("alertReturnedTemplate.html", RMEmailTemplateConstants.ALERT_RETURNED_ACCEPTED, appLayerFactory, migrationResourcesProvider, collection);
			reloadEmailTemplate("alertReactivatedTemplate.html", RMEmailTemplateConstants.ALERT_REACTIVATED_ACCEPTED, appLayerFactory, migrationResourcesProvider, collection);
			reloadEmailTemplate("alertBorrowingExtendedTemplate.html", RMEmailTemplateConstants.ALERT_BORROWING_EXTENTED_ACCEPTED, appLayerFactory, migrationResourcesProvider, collection);
			reloadEmailTemplate("alertBorrowedTemplateDenied.html", RMEmailTemplateConstants.ALERT_BORROWED_DENIED, appLayerFactory, migrationResourcesProvider, collection);
			reloadEmailTemplate("alertReturnedTemplateDenied.html", RMEmailTemplateConstants.ALERT_RETURNED_DENIED, appLayerFactory, migrationResourcesProvider, collection);
			reloadEmailTemplate("alertReactivatedTemplateDenied.html", RMEmailTemplateConstants.ALERT_REACTIVATED_DENIED, appLayerFactory, migrationResourcesProvider, collection);
			reloadEmailTemplate("alertBorrowingExtendedTemplateDenied.html", RMEmailTemplateConstants.ALERT_BORROWING_EXTENTED_DENIED, appLayerFactory, migrationResourcesProvider, collection);
		}
	}

	private void reloadEmailTemplate(final String templateFileName, final String templateId, AppLayerFactory appLayerFactory,
									 MigrationResourcesProvider migrationResourcesProvider, String collection) {
		final InputStream templateInputStream = migrationResourcesProvider.getStream(templateFileName);

		try {
			appLayerFactory.getModelLayerFactory().getEmailTemplatesManager()
					.replaceCollectionTemplate(templateId, collection, templateInputStream);
		} catch (IOException | ConfigManagerException.OptimisticLockingConfiguration e) {
			throw new RuntimeException(e);
		} finally {
			IOUtils.closeQuietly(templateInputStream);
		}
	}

    class SchemaAlterationFor7_2_step1 extends MetadataSchemasAlterationHelper {

        protected SchemaAlterationFor7_2_step1(String collection, MigrationResourcesProvider migrationResourcesProvider,
                                               AppLayerFactory appLayerFactory) {
            super(collection, migrationResourcesProvider, appLayerFactory);
        }

        public String getVersion() {
            return "7.2";
        }

        @Override
        protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
            typesBuilder.getDefaultSchema(Category.SCHEMA_TYPE).deleteMetadataWithoutValidation(typesBuilder.getMetadata(Category.DEFAULT_SCHEMA + "_" + Category.DESCRIPTION));
            typesBuilder.getSchemaType(ContainerRecord.SCHEMA_TYPE).setSecurity(false);
            MetadataBuilder containerStorageSpace = typesBuilder.getDefaultSchema(ContainerRecord.SCHEMA_TYPE).get(ContainerRecord.STORAGE_SPACE);
            typesBuilder.getDefaultSchema(StorageSpace.SCHEMA_TYPE).createUndeletable(StorageSpace.NUMBER_OF_CONTAINERS)
                    .setType(MetadataValueType.NUMBER).defineDataEntry().asReferenceCount(containerStorageSpace).setSearchable(true);
			typesBuilder.getDefaultSchema(ContainerRecord.SCHEMA_TYPE).createUndeletable(ContainerRecord.FIRST_TRANSFER_REPORT_DATE).setType(MetadataValueType.DATE).setSystemReserved(true);
			typesBuilder.getDefaultSchema(ContainerRecord.SCHEMA_TYPE).createUndeletable(ContainerRecord.FIRST_DEPOSIT_REPORT_DATE).setType(MetadataValueType.DATE).setSystemReserved(true);
//			typesBuilder.getDefaultSchema(AdministrativeUnit.SCHEMA_TYPE).createUndeletable(AdministrativeUnit.ADRESS).setType(MetadataValueType.DATE).setSystemReserved(true);
        }
    }

    class SchemaAlterationFor7_2_step2 extends MetadataSchemasAlterationHelper {

        protected SchemaAlterationFor7_2_step2(String collection, MigrationResourcesProvider migrationResourcesProvider,
                                               AppLayerFactory appLayerFactory) {
            super(collection, migrationResourcesProvider, appLayerFactory);
        }

        public String getVersion() {
            return "7.2";
        }

        @Override
        protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
            migrateLabel(typesBuilder);
			migrateMetadatasForRequestEvents(typesBuilder);
			typesBuilder.getSchema(Folder.DEFAULT_SCHEMA).get(Folder.TITLE).setSortable(true);
            typesBuilder.getDefaultSchema(Category.SCHEMA_TYPE).create(Category.DESCRIPTION).setType(MetadataValueType.TEXT);
        }

        private void migrateLabel(MetadataSchemaTypesBuilder typesBuilder) {
            typesBuilder.getSchema(Folder.DEFAULT_SCHEMA).get(Folder.EXPECTED_TRANSFER_DATE).addLabel(Language.French, "Date de transfert prévue");
            typesBuilder.getSchema(Folder.DEFAULT_SCHEMA).get(Folder.EXPECTED_DEPOSIT_DATE).addLabel(Language.French, "Date de versement prévue");
            typesBuilder.getSchema(Folder.DEFAULT_SCHEMA).get(Folder.EXPECTED_DESTRUCTION_DATE).addLabel(Language.French, "Date de destruction prévue");

        }

		public void migrateMetadatasForRequestEvents(MetadataSchemaTypesBuilder typesBuilder) {
			typesBuilder.getDefaultSchema(Folder.SCHEMA_TYPE).createUndeletable(Folder.REACTIVATION_DECOMMISSIONING_DATE)
					.setType(MetadataValueType.DATE);
			typesBuilder.getDefaultSchema(Folder.SCHEMA_TYPE).createUndeletable(Folder.REACTIVATION_DATES)
					.setType(MetadataValueType.DATE).setMultivalue(true);
			typesBuilder.getDefaultSchema(Folder.SCHEMA_TYPE).createUndeletable(Folder.REACTIVATION_USERS)
					.setType(REFERENCE).setMultivalue(true).defineReferencesTo(
					typesBuilder.getSchemaType(User.SCHEMA_TYPE)
			);
			typesBuilder.getDefaultSchema(Folder.SCHEMA_TYPE).createUndeletable(Folder.PREVIOUS_DEPOSIT_DATES)
					.setType(MetadataValueType.DATE).setMultivalue(true);
			typesBuilder.getDefaultSchema(Folder.SCHEMA_TYPE).createUndeletable(Folder.PREVIOUS_TRANSFER_DATES)
					.setType(MetadataValueType.DATE).setMultivalue(true);
			typesBuilder.getDefaultSchema(Folder.SCHEMA_TYPE).get(Folder.DECOMMISSIONING_DATE)
					.defineDataEntry().asCalculated(FolderDecommissioningDateCalculator2.class);

			MetadataSchemaTypeBuilder taskSchemaType = typesBuilder.getSchemaType(Task.SCHEMA_TYPE);
			taskSchemaType.createCustomSchema(BorrowRequest.SCHEMA_NAME);
			taskSchemaType.createCustomSchema(ReturnRequest.SCHEMA_NAME);
			taskSchemaType.createCustomSchema(ReactivationRequest.SCHEMA_NAME);
			taskSchemaType.createCustomSchema(ExtensionRequest.SCHEMA_NAME);

			typesBuilder.getDefaultSchema(ContainerRecord.SCHEMA_TYPE).createUndeletable(ContainerRecord.BORROW_RETURN_DATE)
					.setType(MetadataValueType.DATE_TIME);

			typesBuilder.getSchema(Task.DEFAULT_SCHEMA).createUndeletable(Task.LINKED_CONTAINERS)
					.setType(REFERENCE)
					.defineReferencesTo(typesBuilder.getDefaultSchema(ContainerRecord.SCHEMA_TYPE)).setMultivalue(true);
			typesBuilder.getSchema(Task.DEFAULT_SCHEMA).createUndeletable(Task.REASON).setType(MetadataValueType.TEXT);
			typesBuilder.getSchema(ExtensionRequest.FULL_SCHEMA_NAME).createUndeletable(ExtensionRequest.EXTENSION_VALUE)
					.setType(MetadataValueType.DATE);
			typesBuilder.getSchema(BorrowRequest.FULL_SCHEMA_NAME).createUndeletable(BorrowRequest.BORROW_DURATION)
					.setType(MetadataValueType.NUMBER).setDefaultRequirement(true);
			typesBuilder.getSchema(ReactivationRequest.FULL_SCHEMA_NAME).createUndeletable(ReactivationRequest.REACTIVATION_DATE)
					.setType(MetadataValueType.DATE).setDefaultRequirement(true);
			typesBuilder.getSchema(ExtensionRequest.FULL_SCHEMA_NAME).createUndeletable(ExtensionRequest.ACCEPTED)
					.setType(MetadataValueType.BOOLEAN).setDefaultValue(null);
			typesBuilder.getSchema(BorrowRequest.FULL_SCHEMA_NAME).createUndeletable(BorrowRequest.ACCEPTED)
					.setType(MetadataValueType.BOOLEAN).setDefaultValue(null);
			typesBuilder.getSchema(ReactivationRequest.FULL_SCHEMA_NAME).createUndeletable(ReactivationRequest.ACCEPTED)
					.setType(MetadataValueType.BOOLEAN).setDefaultValue(null);
			typesBuilder.getSchema(ReturnRequest.FULL_SCHEMA_NAME).createUndeletable(ReturnRequest.ACCEPTED)
					.setType(MetadataValueType.BOOLEAN).setDefaultValue(null);
			typesBuilder.getSchema(ExtensionRequest.FULL_SCHEMA_NAME).createUndeletable(ExtensionRequest.APPLICANT)
					.setType(REFERENCE).defineReferencesTo(typesBuilder.getDefaultSchema(User.SCHEMA_TYPE))
					.setSystemReserved(true);
			typesBuilder.getSchema(BorrowRequest.FULL_SCHEMA_NAME).createUndeletable(BorrowRequest.APPLICANT)
					.setType(REFERENCE).defineReferencesTo(typesBuilder.getDefaultSchema(User.SCHEMA_TYPE))
					.setSystemReserved(true);
			typesBuilder.getSchema(ReactivationRequest.FULL_SCHEMA_NAME).createUndeletable(ReactivationRequest.APPLICANT)
					.setType(REFERENCE).defineReferencesTo(typesBuilder.getDefaultSchema(User.SCHEMA_TYPE))
					.setSystemReserved(true);
			typesBuilder.getSchema(ReturnRequest.FULL_SCHEMA_NAME).createUndeletable(ReturnRequest.APPLICANT)
					.setType(REFERENCE).defineReferencesTo(typesBuilder.getDefaultSchema(User.SCHEMA_TYPE))
					.setSystemReserved(true);
			typesBuilder.getSchema(ExtensionRequest.FULL_SCHEMA_NAME).createUndeletable(ExtensionRequest.RESPONDANT)
					.setType(REFERENCE).defineReferencesTo(typesBuilder.getDefaultSchema(User.SCHEMA_TYPE))
					.setSystemReserved(true);
			typesBuilder.getSchema(BorrowRequest.FULL_SCHEMA_NAME).createUndeletable(BorrowRequest.RESPONDANT)
					.setType(REFERENCE).defineReferencesTo(typesBuilder.getDefaultSchema(User.SCHEMA_TYPE))
					.setSystemReserved(true);
			typesBuilder.getSchema(ReactivationRequest.FULL_SCHEMA_NAME).createUndeletable(ReactivationRequest.RESPONDANT)
					.setType(REFERENCE).defineReferencesTo(typesBuilder.getDefaultSchema(User.SCHEMA_TYPE))
					.setSystemReserved(true);
			typesBuilder.getSchema(ReturnRequest.FULL_SCHEMA_NAME).createUndeletable(ReturnRequest.RESPONDANT)
					.setType(REFERENCE).defineReferencesTo(typesBuilder.getDefaultSchema(User.SCHEMA_TYPE))
					.setSystemReserved(true);

			MetadataSchemaTypeBuilder eventSchemaType = typesBuilder.getSchemaType(Event.SCHEMA_TYPE);
			eventSchemaType.getDefaultSchema().create(Event.RECEIVER_NAME).setType(REFERENCE)
					.defineReferencesTo(typesBuilder.getSchemaType(User.SCHEMA_TYPE));
			eventSchemaType.getDefaultSchema().create(Event.TASK).setType(REFERENCE)
					.defineReferencesTo(typesBuilder.getSchemaType(Task.SCHEMA_TYPE));
			eventSchemaType.getDefaultSchema().create(Event.DESCRIPTION).setType(MetadataValueType.TEXT);
			eventSchemaType.getDefaultSchema().create(Event.ACCEPTED).setType(MetadataValueType.BOOLEAN);
		}
    }
}

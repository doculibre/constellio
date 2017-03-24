package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.RMEmailTemplateConstants;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.services.TasksSchemasRecordsServices;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.dao.managers.config.ConfigManagerException;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

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
        transaction.add(taskSchemas.newTaskType().setCode("borrowRequest").setTitle("Demande d'emprunt").setLinkedSchema("userTask_borrowRequest"));
        transaction.add(taskSchemas.newTaskType().setCode("returnRequest").setTitle("Demande de retour").setLinkedSchema("userTask_returnRequest"));
        transaction.add(taskSchemas.newTaskType().setCode("reactivationRequest").setTitle("Demande de réactivation").setLinkedSchema("userTask_reactivationRequest"));
        transaction.add(taskSchemas.newTaskType().setCode("borrowExtensionRequest").setTitle("Demande de prolongation d'emprunt").setLinkedSchema("userTask_borrowExtensionRequest"));
        appLayerFactory.getModelLayerFactory().newRecordServices().execute(transaction);
        reloadEmailTemplates();
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
            taskSchemaType.createCustomSchema("borrowRequest");
            taskSchemaType.createCustomSchema("returnRequest");
            taskSchemaType.createCustomSchema("reactivationRequest");
            taskSchemaType.createCustomSchema("borrowExtensionRequest");

            typesBuilder.getDefaultSchema(ContainerRecord.SCHEMA_TYPE).createUndeletable(ContainerRecord.BORROW_RETURN_DATE)
                    .setType(MetadataValueType.DATE_TIME);
        }
    }
}

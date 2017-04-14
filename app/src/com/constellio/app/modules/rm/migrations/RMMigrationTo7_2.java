package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.entities.schemasDisplay.enums.MetadataInputType;
import com.constellio.app.modules.rm.constants.RMRoles;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.StorageSpace;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.data.utils.BatchBuilderIterator;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.RecordUpdateOptions;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            typesBuilder.getSchema(Folder.DEFAULT_SCHEMA).get(Folder.TITLE).setSortable(true);
            typesBuilder.getDefaultSchema(Category.SCHEMA_TYPE).create(Category.DESCRIPTION).setType(MetadataValueType.TEXT);
        }

        private void migrateLabel(MetadataSchemaTypesBuilder typesBuilder) {
            typesBuilder.getSchema(Folder.DEFAULT_SCHEMA).get(Folder.EXPECTED_TRANSFER_DATE).addLabel(Language.French, "Date de transfert prévue");
            typesBuilder.getSchema(Folder.DEFAULT_SCHEMA).get(Folder.EXPECTED_DEPOSIT_DATE).addLabel(Language.French, "Date de versement prévue");
            typesBuilder.getSchema(Folder.DEFAULT_SCHEMA).get(Folder.EXPECTED_DESTRUCTION_DATE).addLabel(Language.French, "Date de destruction prévue");

        }
    }
}

package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.StorageSpace;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

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
        new SchemaAlterationFor7_2(collection, migrationResourcesProvider, appLayerFactory).migrate();
        migrateSearchableSchemaTypes(collection, migrationResourcesProvider, appLayerFactory);
    }

    private void migrateSearchableSchemaTypes(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory) {
        SchemasDisplayManager manager = appLayerFactory.getMetadataSchemasDisplayManager();
        manager.saveType(manager.getType(collection, ContainerRecord.SCHEMA_TYPE).withSimpleAndAdvancedSearchStatus(true));
        manager.saveType(manager.getType(collection, StorageSpace.SCHEMA_TYPE).withSimpleAndAdvancedSearchStatus(true));

        manager.saveMetadata(manager.getMetadata(collection, StorageSpace.DEFAULT_SCHEMA + "_" + StorageSpace.NUMBER_OF_CONTAINERS).withVisibleInAdvancedSearchStatus(true));
    }

    class SchemaAlterationFor7_2 extends MetadataSchemasAlterationHelper {

        protected SchemaAlterationFor7_2(String collection, MigrationResourcesProvider migrationResourcesProvider,
                                           AppLayerFactory appLayerFactory) {
            super(collection, migrationResourcesProvider, appLayerFactory);
        }

        public String getVersion() {
            return "7.2";
        }

        @Override
        protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
            typesBuilder.getSchemaType(ContainerRecord.SCHEMA_TYPE).setSecurity(false);
            MetadataBuilder containerStorageSpace = typesBuilder.getDefaultSchema(ContainerRecord.SCHEMA_TYPE).get(ContainerRecord.STORAGE_SPACE);
            typesBuilder.getDefaultSchema(StorageSpace.SCHEMA_TYPE).createUndeletable(StorageSpace.NUMBER_OF_CONTAINERS)
                    .setType(MetadataValueType.NUMBER).defineDataEntry().asReferenceCount(containerStorageSpace).setSearchable(true);
        }
    }
}

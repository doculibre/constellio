package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.model.calculators.container.ContainerRecordAvailableSizeCalculator;
import com.constellio.app.modules.rm.model.calculators.container.ContainerRecordLinearSizeCalculator;
import com.constellio.app.modules.rm.model.validators.ContainerRecordValidator;
import com.constellio.app.modules.rm.model.validators.StorageSpaceValidator;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.StorageSpace;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class RMMigrationTo6_7 implements MigrationScript {
    @Override
    public String getVersion() {
        return "6.7";
    }

    @Override
    public void migrate(String collection, MigrationResourcesProvider provider, AppLayerFactory factory)
            throws Exception {
        new SchemaAlterationsFor6_7(collection, provider, factory).migrate();
    }

    public static class SchemaAlterationsFor6_7 extends MetadataSchemasAlterationHelper {

        protected SchemaAlterationsFor6_7(String collection, MigrationResourcesProvider provider, AppLayerFactory factory) {
            super(collection, provider, factory);
        }

        @Override
        protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
            buildContainerRecordMetadatas(typesBuilder);
            buildStorageSpaceMetadatas(typesBuilder);
        }

        private void buildContainerRecordMetadatas(MetadataSchemaTypesBuilder typesBuilder) {
            typesBuilder.getSchema(ContainerRecord.DEFAULT_SCHEMA).defineValidators().add(ContainerRecordValidator.class);

            typesBuilder.getDefaultSchema(ContainerRecord.SCHEMA_TYPE).create(ContainerRecord.LINEAR_SIZE_ENTERED)
                    .setType(MetadataValueType.NUMBER).setEssential(false).setUndeletable(true);

            typesBuilder.getDefaultSchema(ContainerRecord.SCHEMA_TYPE).create(ContainerRecord.LINEAR_SIZE_SUM)
                    .setType(MetadataValueType.NUMBER).setEssential(false).setUndeletable(true).defineDataEntry().asSum(
                    typesBuilder.getDefaultSchema(Folder.SCHEMA_TYPE).getMetadata(Folder.CONTAINER),
                    typesBuilder.getDefaultSchema(Folder.SCHEMA_TYPE).getMetadata(Folder.LINEAR_SIZE)
            );

            typesBuilder.getDefaultSchema(ContainerRecord.SCHEMA_TYPE).create(ContainerRecord.LINEAR_SIZE)
                    .setType(MetadataValueType.NUMBER).setEssential(false).setUndeletable(true)
                    .defineDataEntry().asCalculated(ContainerRecordLinearSizeCalculator.class);

            typesBuilder.getDefaultSchema(ContainerRecord.SCHEMA_TYPE).create(ContainerRecord.AVAILABLE_SIZE)
                    .setType(MetadataValueType.NUMBER).setEssential(false).setUndeletable(true)
                    .defineDataEntry().asCalculated(ContainerRecordAvailableSizeCalculator.class);

            typesBuilder.getDefaultSchema(ContainerRecord.SCHEMA_TYPE).getMetadata(ContainerRecord.FILL_RATIO_ENTRED).setEnabled(false);
        }

        private void buildStorageSpaceMetadatas(MetadataSchemaTypesBuilder typesBuilder) {
            typesBuilder.getSchema(StorageSpace.DEFAULT_SCHEMA).defineValidators().add(StorageSpaceValidator.class);
        }
    }
}

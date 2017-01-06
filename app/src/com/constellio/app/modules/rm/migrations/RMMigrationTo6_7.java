package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.model.calculators.container.ContainerRecordAvailableSizeCalculator;
import com.constellio.app.modules.rm.model.calculators.container.ContainerRecordLinearSizeCalculator;
import com.constellio.app.modules.rm.model.calculators.storageSpace.StorageSpaceAvailableSizeCalculator;
import com.constellio.app.modules.rm.model.calculators.storageSpace.StorageSpaceLinearSizeCalculator;
import com.constellio.app.modules.rm.model.enums.FolderStatus;
import com.constellio.app.modules.rm.model.validators.ContainerRecordValidator;
import com.constellio.app.modules.rm.model.validators.StorageSpaceValidator;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.decommissioning.SearchType;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.StorageSpace;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

import java.util.List;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.ALL;

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
            migrateContainerRecordMetadatas(typesBuilder);
            migrateStorageSpaceMetadatas(typesBuilder);
        }

        private void migrateDecommissioningListMetadatas(MetadataSchemaTypesBuilder typesBuilder) {
            typesBuilder.getDefaultSchema(DecommissioningList.SCHEMA_TYPE).create(DecommissioningList.SEARCH_TYPE)
                    .setType(MetadataValueType.ENUM).setEssential(true).setUndeletable(true);

            RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
            List<DecommissioningList> decommissioningLists = rm.searchDecommissioningLists(ALL);
            for(DecommissioningList decommissioningList: decommissioningLists) {
                if(decommissioningList.getSearchType() == null) {
                    FolderStatus folderStatus = null;
                    if(!decommissioningList.getFolderDetails().isEmpty()) {
                        folderStatus = new RMSchemasRecordsServices(collection, appLayerFactory).getFolder(decommissioningList.getFolderDetails().get(0).getFolderId()).getArchivisticStatus();
                    }

                    if(folderStatus != null) {
                        if(folderStatus.isActive()) {
                            switch (decommissioningList.getDecommissioningListType()) {
                                case FOLDERS_TO_DEPOSIT:
                                    break;
                                case FOLDERS_TO_DESTROY:
                                    break;
                                case FOLDERS_TO_TRANSFER:
                                    break;
                                case DOCUMENTS_TO_DEPOSIT:
                                    break;
                                case DOCUMENTS_TO_DESTROY:
                                    break;
                                case DOCUMENTS_TO_TRANSFER:
                                    break;
                            }
                        } else if(folderStatus.isSemiActive()) {
                            switch (decommissioningList.getDecommissioningListType()) {
                                case FOLDERS_TO_DEPOSIT:
                                    break;
                                case FOLDERS_TO_DESTROY:
                                    break;
                                case DOCUMENTS_TO_DEPOSIT:
                                    break;
                                case DOCUMENTS_TO_DESTROY:
                                    break;
                            }
                        }
                    }
                }
            }
        }

        private SearchType calculateDecommissioningListSearchType(DecommissioningList decommissioningList) {
            return null;
        }

        private void migrateContainerRecordMetadatas(MetadataSchemaTypesBuilder typesBuilder) {
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

        private void migrateStorageSpaceMetadatas(MetadataSchemaTypesBuilder typesBuilder) {
            typesBuilder.getSchema(StorageSpace.DEFAULT_SCHEMA).defineValidators().add(StorageSpaceValidator.class);

            typesBuilder.getDefaultSchema(StorageSpace.SCHEMA_TYPE).create(StorageSpace.LINEAR_SIZE_ENTERED)
                    .setType(MetadataValueType.NUMBER).setEssential(false).setUndeletable(true);

            typesBuilder.getDefaultSchema(StorageSpace.SCHEMA_TYPE).create(StorageSpace.LINEAR_SIZE_SUM)
                    .setType(MetadataValueType.NUMBER).setEssential(false).setUndeletable(true).defineDataEntry().asSum(
                    typesBuilder.getDefaultSchema(ContainerRecord.SCHEMA_TYPE).getMetadata(ContainerRecord.STORAGE_SPACE),
                    typesBuilder.getDefaultSchema(ContainerRecord.SCHEMA_TYPE).getMetadata(ContainerRecord.LINEAR_SIZE)
            );

            typesBuilder.getDefaultSchema(StorageSpace.SCHEMA_TYPE).create(StorageSpace.LINEAR_SIZE)
                    .setType(MetadataValueType.NUMBER).setEssential(false).setUndeletable(true)
                    .defineDataEntry().asCalculated(StorageSpaceLinearSizeCalculator.class);

            typesBuilder.getDefaultSchema(StorageSpace.SCHEMA_TYPE).create(StorageSpace.AVAILABLE_SIZE)
                    .setType(MetadataValueType.NUMBER).setEssential(false).setUndeletable(true)
                    .defineDataEntry().asCalculated(StorageSpaceAvailableSizeCalculator.class);
        }
    }
}

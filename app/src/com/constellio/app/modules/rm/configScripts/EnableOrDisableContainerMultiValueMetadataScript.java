package com.constellio.app.modules.rm.configScripts;

import com.constellio.app.modules.rm.model.calculators.container.ContainerRecordLocalizationCalculator;
import com.constellio.app.modules.rm.model.calculators.storageSpace.StorageSpaceAvailableSizeCalculator;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.StorageSpace;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.model.entities.configs.AbstractSystemConfigurationScript;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

import java.util.List;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

/**
 * Created by constellios on 2017-04-25.
 */
public class EnableOrDisableContainerMultiValueMetadataScript extends
        AbstractSystemConfigurationScript<Boolean> {

    public static final String CONTAINER_EXIST = "containerExist";

    @Override
    public void validate(Boolean newValue, ValidationErrors errors) {
        AppLayerFactory appLayerFactory = ConstellioFactories.getInstance().getAppLayerFactory();
        List<String> listCollectionCode = appLayerFactory.getCollectionsManager().getCollectionCodes();
        RMSchemasRecordsServices rm;
        SearchServices searchServices = appLayerFactory.getModelLayerFactory().newSearchServices();
        MetadataSchemasManager metadataSchemasManager = appLayerFactory.getModelLayerFactory().getMetadataSchemasManager();
        boolean hasContainer;

        for(String code : listCollectionCode)
        {
            if(metadataSchemasManager.getSchemaTypes(code).hasType(ContainerRecord.SCHEMA_TYPE))
            {
                rm = new RMSchemasRecordsServices(code, appLayerFactory);
                LogicalSearchQuery query = new LogicalSearchQuery(from(rm.containerRecord.schemaType()).returnAll());

                hasContainer = searchServices.hasResults(query);
                if(hasContainer)
                {
                    errors.add(getClass(), CONTAINER_EXIST);
                    break;
                }
            }
        }
    }

    @Override
    public void onValueChanged(Boolean previousValue, Boolean newValue, ModelLayerFactory modelLayerFactory) {
        AppLayerFactory appLayerFactory = ConstellioFactories.getInstance().getAppLayerFactory();

        if (newValue == null) {
            newValue = Boolean.FALSE;
        }
        if (previousValue == null) {
            previousValue = Boolean.FALSE;
        }
        if(newValue != previousValue) {
            CollectionsListManager collectionManager = modelLayerFactory
                    .getCollectionsListManager();
            for (String collection : collectionManager.getCollectionsExcludingSystem()) {
                onValueChangedForCollection(newValue, modelLayerFactory, collection);
            }
        }
    }

    private void onValueChangedForCollection(final Boolean newValue, ModelLayerFactory modelLayerFactory,
                                             String collection) {
        MetadataSchemasManager metadataSchemasManager = modelLayerFactory.getMetadataSchemasManager();

        if (metadataSchemasManager.getSchemaTypes(collection).hasType(ContainerRecord.SCHEMA_TYPE)) {

            metadataSchemasManager.modify(collection, new MetadataSchemaTypesAlteration() {
                @Override
                public void alter(MetadataSchemaTypesBuilder types) {
                    MetadataSchemaBuilder metadataSchemaBuilder = types.getSchema(ContainerRecord.DEFAULT_SCHEMA);
                    metadataSchemaBuilder.get(ContainerRecord.STORAGE_SPACE).setMultivalue(newValue);
                }
            });

            if(Boolean.TRUE.equals(newValue)) {
                enableContainerMultivalueMetadata(modelLayerFactory, collection);
            } else {
                disableContainerMultivalueMetadata(modelLayerFactory, collection);
            }
        }
    }

    private void disableContainerMultivalueMetadata(ModelLayerFactory modelLayerFactory, String collection) {
        MetadataSchemasManager metadataSchemasManager = modelLayerFactory.getMetadataSchemasManager();

        metadataSchemasManager.modify(collection, new MetadataSchemaTypesAlteration() {
            @Override
            public void alter(MetadataSchemaTypesBuilder types) {
                types.getSchema(StorageSpace.DEFAULT_SCHEMA).get(StorageSpace.AVAILABLE_SIZE).defineDataEntry()
                        .asCalculated(StorageSpaceAvailableSizeCalculator.class);
                types.getSchema(StorageSpace.DEFAULT_SCHEMA).get(StorageSpace.LINEAR_SIZE_SUM).defineDataEntry()
                        .asSum(
                                types.getDefaultSchema(ContainerRecord.SCHEMA_TYPE).getMetadata(ContainerRecord.STORAGE_SPACE),
                                types.getDefaultSchema(ContainerRecord.SCHEMA_TYPE).getMetadata(ContainerRecord.CAPACITY)
                        );;
                types.getSchema(ContainerRecord.DEFAULT_SCHEMA).get(ContainerRecord.LOCALIZATION).defineDataEntry()
                        .asCalculated(ContainerRecordLocalizationCalculator.class);
            }
        });
    }

    private void enableContainerMultivalueMetadata(ModelLayerFactory modelLayerFactory, String collection) {
        MetadataSchemasManager metadataSchemasManager = modelLayerFactory.getMetadataSchemasManager();

        metadataSchemasManager.modify(collection, new MetadataSchemaTypesAlteration() {
            @Override
            public void alter(MetadataSchemaTypesBuilder types) {
                types.getSchema(StorageSpace.DEFAULT_SCHEMA).get(StorageSpace.AVAILABLE_SIZE).defineDataEntry()
                        .asManual();
                types.getSchema(StorageSpace.DEFAULT_SCHEMA).get(StorageSpace.LINEAR_SIZE_SUM).defineDataEntry()
                        .asManual();
                types.getSchema(ContainerRecord.DEFAULT_SCHEMA).get(ContainerRecord.LOCALIZATION).defineDataEntry()
                        .asManual();
            }
        });
    }
}

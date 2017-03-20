package com.constellio.app.modules.rm.configScripts;

import com.constellio.app.modules.rm.model.calculators.storageSpace.StorageSpaceTitleCalculator;
import com.constellio.app.modules.rm.wrappers.StorageSpace;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.model.entities.configs.AbstractSystemConfigurationScript;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class EnableOrDisableStorageSpaceTitleCalculatorScript extends
        AbstractSystemConfigurationScript<Boolean> {

    @Override
    public void onValueChanged(Boolean previousValue, Boolean newValue, ModelLayerFactory modelLayerFactory) {
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

    private void onValueChangedForCollection(Boolean newValue, ModelLayerFactory modelLayerFactory,
                                             String collection) {
        if(Boolean.TRUE.equals(newValue)) {
            enableUniformSubdivisionTitleCalculator(modelLayerFactory, collection);
        } else {
            disableUniformSubdivisionTitleCalculator(modelLayerFactory, collection);
        }
    }

    private void disableUniformSubdivisionTitleCalculator(ModelLayerFactory modelLayerFactory,
                                                           String collection) {
        MetadataSchemasManager metadataSchemasManager = modelLayerFactory.getMetadataSchemasManager();
        metadataSchemasManager.modify(collection, new MetadataSchemaTypesAlteration() {
            @Override
            public void alter(MetadataSchemaTypesBuilder types) {
                types.getDefaultSchema(StorageSpace.SCHEMA_TYPE).get(StorageSpace.TITLE).setUniqueValue(false).defineDataEntry().asManual();
                types.getDefaultSchema(StorageSpace.SCHEMA_TYPE).get(StorageSpace.CODE).setUniqueValue(true);
            }
        });
    }

    private void enableUniformSubdivisionTitleCalculator(ModelLayerFactory modelLayerFactory,
                                                          String collection) {
        MetadataSchemasManager metadataSchemasManager = modelLayerFactory.getMetadataSchemasManager();
        metadataSchemasManager.modify(collection, new MetadataSchemaTypesAlteration() {
            @Override
            public void alter(MetadataSchemaTypesBuilder types) {
                types.getDefaultSchema(StorageSpace.SCHEMA_TYPE).get(StorageSpace.TITLE).setUniqueValue(true).defineDataEntry().asCalculated(StorageSpaceTitleCalculator.class);
                types.getDefaultSchema(StorageSpace.SCHEMA_TYPE).get(StorageSpace.CODE).setUniqueValue(false);
            }
        });
        ConstellioFactories.getInstance().getAppLayerFactory().getSystemGlobalConfigsManager().setReindexingRequired(true);
    }
}

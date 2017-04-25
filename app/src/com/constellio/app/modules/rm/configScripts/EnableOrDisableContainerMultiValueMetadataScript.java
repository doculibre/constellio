package com.constellio.app.modules.rm.configScripts;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.model.entities.configs.AbstractSystemConfigurationScript;
import com.constellio.model.frameworks.validation.ValidationErrors;
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

    public static final String CONTAINER_EXIST = "containerExisit";

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
    public void onValueChanged(Boolean previousValue, final Boolean newValue, ModelLayerFactory modelLayerFactory) {
        AppLayerFactory appLayerFactory = ConstellioFactories.getInstance().getAppLayerFactory();
        List<String> listCollectionCode = appLayerFactory.getCollectionsManager().getCollectionCodes();

        MetadataSchemasManager metadataSchemasManager = appLayerFactory.getModelLayerFactory().getMetadataSchemasManager();

        for(String code : listCollectionCode) {
            if (metadataSchemasManager.getSchemaTypes(code).hasType(ContainerRecord.SCHEMA_TYPE)) {

                metadataSchemasManager.modify(code, new MetadataSchemaTypesAlteration() {
                    @Override
                    public void alter(MetadataSchemaTypesBuilder types) {
                        MetadataSchemaBuilder metadataSchemaBuilder = types.getSchema(ContainerRecord.DEFAULT_SCHEMA);
                        metadataSchemaBuilder.get(ContainerRecord.STORAGE_SPACE).setMultivalue(newValue);
                    }
                });
            }
        }
    }
}

package com.constellio.app.modules.rm.extensions.schema;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.type.MediumType;
import com.constellio.data.frameworks.extensions.ExtensionBooleanResult;
import com.constellio.model.extensions.behaviors.RecordExtension;
import com.constellio.model.extensions.events.records.RecordLogicalDeletionValidationEvent;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.search.SearchServices;

import static com.constellio.model.entities.schemas.Schemas.CODE;

/**
 * Created by Charles Blanchette on 2017-03-22.
 */
public class RMMediumTypeRecordExtension extends RecordExtension {

    String collection;

    ModelLayerFactory modelLayerFactory;

    RMSchemasRecordsServices rm;

    public RMMediumTypeRecordExtension(String collection, ModelLayerFactory modelLayerFactory) {
        this.modelLayerFactory = modelLayerFactory;
        this.collection = collection;
        this.rm = new RMSchemasRecordsServices(collection, modelLayerFactory);
    }

    @Override
    public ExtensionBooleanResult isLogicallyDeletable(RecordLogicalDeletionValidationEvent event) {
        if (MediumType.SCHEMA_TYPE.equals(event.getSchemaTypeCode())
                && event.getRecord().get(CODE).equals("DM")) {
            return ExtensionBooleanResult.FALSE;
        }
        return super.isLogicallyDeletable(event);
    }
}

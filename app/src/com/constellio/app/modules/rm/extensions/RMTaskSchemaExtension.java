package com.constellio.app.modules.rm.extensions;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.model.wrappers.request.BorrowRequest;
import com.constellio.app.modules.tasks.model.wrappers.request.ExtensionRequest;
import com.constellio.app.modules.tasks.model.wrappers.request.ReactivationRequest;
import com.constellio.app.modules.tasks.model.wrappers.request.ReturnRequest;
import com.constellio.data.frameworks.extensions.ExtensionBooleanResult;
import com.constellio.model.extensions.behaviors.RecordExtension;
import com.constellio.model.extensions.events.records.RecordLogicalDeletionValidationEvent;
import com.constellio.model.services.factories.ModelLayerFactory;

/**
 * Created by Charles Blanchette on 2017-03-30.
 */
public class RMTaskSchemaExtension extends RecordExtension {

    String collection;

    ModelLayerFactory modelLayerFactory;

    RMSchemasRecordsServices rm;

    public RMTaskSchemaExtension(String collection, ModelLayerFactory modelLayerFactory) {
        this.modelLayerFactory = modelLayerFactory;
        this.collection = collection;
        this.rm = new RMSchemasRecordsServices(collection, modelLayerFactory);
    }

    @Override
    public ExtensionBooleanResult isLogicallyDeletable(RecordLogicalDeletionValidationEvent event) {
        String schemaExtensionRequest = Task.SCHEMA_TYPE + "_" + ExtensionRequest.SCHEMA_NAME;
        String schemaBorrowRequest = Task.SCHEMA_TYPE + "_" + BorrowRequest.SCHEMA_NAME;
        String schemaReactivationRequest = Task.SCHEMA_TYPE + "_" + ReactivationRequest.SCHEMA_NAME;
        String schemaReturnRequest = Task.SCHEMA_TYPE + "_" + ReturnRequest.SCHEMA_NAME;

        if (Task.SCHEMA_TYPE.equals(event.getSchemaTypeCode())) {
            return ExtensionBooleanResult.FALSE;
        }
        return super.isLogicallyDeletable(event);
    }
}

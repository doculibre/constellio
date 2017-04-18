package com.constellio.app.modules.rm.wrappers;

import com.constellio.app.modules.tasks.model.wrappers.types.TaskType;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

/**
 * Created by Charles Blanchette on 2017-03-30.
 */
public class RMTaskType extends TaskType {

    public static final String BORROW_REQUEST = "borrowRequest";
    public static final String RETURN_REQUEST = "returnRequest";
    public static final String REACTIVATION_REQUEST = "reactivationRequest";
    public static final String BORROW_EXTENSION_REQUEST = "borrowExtensionRequest";

    public RMTaskType(Record record, MetadataSchemaTypes types) {
        super(record, types);
    }
}

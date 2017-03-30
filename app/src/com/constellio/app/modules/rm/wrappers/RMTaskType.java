package com.constellio.app.modules.rm.wrappers;

import com.constellio.app.modules.tasks.model.wrappers.types.TaskType;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

/**
 * Created by constellios on 2017-03-30.
 */
public class RMTaskType extends TaskType {

    public static final String BORROW_REQUEST = "borrowRequest";

    public RMTaskType(Record record, MetadataSchemaTypes types) {
        super(record, types);
    }
}

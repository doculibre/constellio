package com.constellio.app.modules.tasks.model.wrappers.request;

import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

/**
 * Created by Marco on 2017-03-28.
 */
public class ReactivationRequest extends Task {

    public static final String SCHEMA_NAME = "reactivationRequest";

    public ReactivationRequest(Record record, MetadataSchemaTypes types) {
        super(record, types);
    }
}

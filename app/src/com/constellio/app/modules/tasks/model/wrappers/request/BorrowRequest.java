package com.constellio.app.modules.tasks.model.wrappers.request;

import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

/**
 * Created by Marco on 2017-03-28.
 */
public class BorrowRequest extends RequestTask {

    public static final String SCHEMA_NAME = "borrowRequest";
    public static final String FULL_SCHEMA_NAME = Task.SCHEMA_TYPE + "_" + SCHEMA_NAME;

    public BorrowRequest(Record record, MetadataSchemaTypes types) {
        super(record, types);
    }

    public String getCompletedBy() {
        return get(COMPLETED_BY);
    }

    public BorrowRequest setCompletedBy(String user) {
        set(COMPLETED_BY, user);
        return this;
    }
}

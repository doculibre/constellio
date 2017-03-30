package com.constellio.app.modules.tasks.model.wrappers.request;

import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

/**
 * Created by Marco on 2017-03-28.
 */
public class BorrowRequest extends Task {

    public static final String SCHEMA_NAME = "borrowRequest";
    public static final String ACCEPTED = "accepted";
    public static final String COMPLETED_BY = "completedBy";

    public BorrowRequest(Record record, MetadataSchemaTypes types) {
        super(record, types);
    }

    public boolean isAccepted() {
        return Boolean.TRUE.equals(get(ACCEPTED));
    }

    public BorrowRequest setAccepted(boolean accepted) {
        set(ACCEPTED, accepted);
        return this;
    }

    public String getCompletedBy() {
        return get(COMPLETED_BY);
    }

    public BorrowRequest setCompletedBy(String user) {
        set(COMPLETED_BY, user);
        return this;
    }
}

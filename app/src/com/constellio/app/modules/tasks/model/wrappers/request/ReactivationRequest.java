package com.constellio.app.modules.tasks.model.wrappers.request;

import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

/**
 * Created by Marco on 2017-03-28.
 */
public class ReactivationRequest extends Task {

    public static final String SCHEMA_NAME = "reactivationRequest";
    public static final String ACCEPTED = "accepted";

    public ReactivationRequest(Record record, MetadataSchemaTypes types) {
        super(record, types);
    }

    public boolean isAccepted() {
        return Boolean.TRUE.equals(get(ACCEPTED));
    }

    public ReactivationRequest setAccepted(boolean accepted) {
        set(ACCEPTED, accepted);
        return this;
    }

    public String getCompletedBy() {
        return get(COMPLETED_BY);
    }

    public ReactivationRequest setCompletedBy(String user) {
        set(COMPLETED_BY, user);
        return this;
    }
}

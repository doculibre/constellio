package com.constellio.app.modules.tasks.model.wrappers.request;

import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

/**
 * Created by Marco on 2017-03-28.
 */
public class ExtensionRequest extends Task {

    public static final String SCHEMA_NAME = "borrowExtensionRequest";
    public static final String EXTENSION_VALUE = "extensionValue";
    public static final String ACCEPTED = "accepted";

    public ExtensionRequest(Record record, MetadataSchemaTypes types) {
        super(record, types);
    }

    public boolean isAccepted() {
        return Boolean.TRUE.equals(get(ACCEPTED));
    }

    public ExtensionRequest setAccepted(boolean accepted) {
        set(ACCEPTED, accepted);
        return this;
    }

    public String getCompletedBy() {
        return get(COMPLETED_BY);
    }

    public ExtensionRequest setCompletedBy(String user) {
        set(COMPLETED_BY, user);
        return this;
    }
}

package com.constellio.app.modules.tasks.model.wrappers.request;

import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

/**
 * Created by Marco on 2017-03-28.
 */
public class ExtensionRequest extends RequestTask {

    public static final String SCHEMA_NAME = "borrowExtensionRequest";
    public static final String FULL_SCHEMA_NAME = Task.SCHEMA_TYPE + "_" + SCHEMA_NAME;
    public static final String EXTENSION_VALUE = "extensionValue";

    public ExtensionRequest(Record record, MetadataSchemaTypes types) {
        super(record, types);
    }

    public String getCompletedBy() {
        return get(COMPLETED_BY);
    }

    public ExtensionRequest setCompletedBy(String user) {
        set(COMPLETED_BY, user);
        return this;
    }
}

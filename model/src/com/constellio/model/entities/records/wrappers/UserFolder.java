package com.constellio.model.entities.records.wrappers;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

/**
 * Created by Nicolas D'Amours
 */
public class UserFolder extends RecordWrapper {
    public static final String SCHEMA_TYPE = "userFolder";

    public static final String DEFAULT_SCHEMA = SCHEMA_TYPE + "_default";

    public static final String PARENT_USER_FOLDER = "parentUserFolder";


    public UserFolder(Record record, MetadataSchemaTypes types) {
        super(record, types, SCHEMA_TYPE);
    }

    public UserFolder getParent() {
        return get(PARENT_USER_FOLDER);
    }

    public UserFolder setParent(UserFolder userFolder) {
        set(PARENT_USER_FOLDER, userFolder);
        return this;
    }
}

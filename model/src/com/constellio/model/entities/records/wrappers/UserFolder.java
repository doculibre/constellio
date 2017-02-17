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

    public static final String PARENT = "parent";


    public UserFolder(Record record, MetadataSchemaTypes types, String typeRequirement) {
        super(record, types, typeRequirement);
    }

    public UserFolder getParent() {
        return get(PARENT);
    }

    public UserFolder setParent(UserFolder userFolder) {
        set(PARENT, userFolder);
        return this;
    }
}

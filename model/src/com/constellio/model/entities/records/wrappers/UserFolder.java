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

    public static final String USER_FOLDER = "userFolder";


    public UserFolder(Record record, MetadataSchemaTypes types, String typeRequirement) {
        super(record, types, typeRequirement);
    }

    public UserFolder getUserFolder() {
        return get(USER_FOLDER);
    }

    public UserFolder setUserFolder(UserFolder userFolder) {
        set(USER_FOLDER, userFolder);
        return this;
    }
}

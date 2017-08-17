package com.constellio.app.modules.rm.wrappers;

import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import org.joda.time.LocalDateTime;

public class SIParchive extends RecordWrapper {
    public static final String SCHEMA_TYPE = "sipArchive";

    public static final String DEFAULT_SCHEMA = SCHEMA_TYPE + "_default";

    public static final String NAME = "name";

    public static final String CREATION_DATE = "creationDate";

    public static final String CONTENT = "content";

    public static final String USER = "user";

    public SIParchive(Record record, MetadataSchemaTypes types) {
        super(record, types, SCHEMA_TYPE);
    }

    public String getName(){
        return get(NAME);
    }

    public SIParchive setName(String name) {
        set(NAME, name);
        return this;
    }

    public LocalDateTime getCreationDate() {
        return get(CREATION_DATE);
    }

    public SIParchive setCreationDate(LocalDateTime localDateTime) {
        set(CREATION_DATE, localDateTime);
        return this;
    }

    public Content getContent(){
        return get(CONTENT);
    }

    public SIParchive setContent(Content content) {
        set(CONTENT, content);
        return this;
    }
}

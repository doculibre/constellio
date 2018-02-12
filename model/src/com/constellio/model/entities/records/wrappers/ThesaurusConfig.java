package com.constellio.model.entities.records.wrappers;

import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

public class ThesaurusConfig extends RecordWrapper {
    public static final String SCHEMA_TYPE = "thesaurusConfig";
    public static final String DEFAULT_SCHEMA = SCHEMA_TYPE + "_default";
    public static final String CONTENT = "content";

    public ThesaurusConfig(Record record, MetadataSchemaTypes types, String typeRequirement) {
        super(record, types, typeRequirement);
    }

    public Content getContent() {
        return get(CONTENT);
    }

    public void setContent(Content content) {
        set(CONTENT, content);
    }
}

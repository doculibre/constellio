package com.constellio.model.entities.records.wrappers;

import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

import java.util.List;

public class ThesaurusConfig extends RecordWrapper {
    public static final String SCHEMA_TYPE = "thesaurusConfig";
    public static final String DEFAULT_SCHEMA = SCHEMA_TYPE + "_default";
    public static final String CONTENT = "content";
    public static final String DENINED_WORDS = "deniedWord";


    public ThesaurusConfig(Record record, MetadataSchemaTypes types) {
        super(record, types, SCHEMA_TYPE);
    }

    public Content getContent() {
        return get(CONTENT);
    }

    public void setContent(Content content) {
        set(CONTENT, content);
    }

    public List<String> getDenidedWords() {
        return get(DENINED_WORDS);
    }

    public void setDenidedWords(List<String> denidedWords) {
        set(DENINED_WORDS, denidedWords);
    }
}

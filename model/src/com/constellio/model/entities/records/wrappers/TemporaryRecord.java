package com.constellio.model.entities.records.wrappers;

import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.Schemas;
import org.joda.time.LocalDateTime;

public class TemporaryRecord extends RecordWrapper {


    public static final String SCHEMA_TYPE = "temporaryRecord";
    public static final String DEFAULT_SCHEMA = SCHEMA_TYPE + "_default";
    public static final String DESTRUCTION_DATE = "destructionDate";
    public static final String DAY_BEFORE_DESTRUCTION = "daysBeforeDestruction";
    public static final String TITLE = "title";
    public static final String CONTENT = "content";

    public TemporaryRecord(Record record,
                           MetadataSchemaTypes types) {
        super(record, types, SCHEMA_TYPE);
    }

    public TemporaryRecord(Record record, MetadataSchemaTypes types, String schema) {
        super(record, types, schema);
    }

    public LocalDateTime getCreatedOn() {
        return get(Schemas.CREATED_ON);
    }

    public TemporaryRecord setCreatedOn(LocalDateTime creationDate) {
        set(Schemas.CREATED_ON, creationDate);
        return this;
    }

    public LocalDateTime getDestructionDate() {
        return get(DESTRUCTION_DATE);
    }

    public TemporaryRecord setDestructionDate(LocalDateTime destructionDate) {
        set(DESTRUCTION_DATE, destructionDate);
        return this;
    }

    public TemporaryRecord setContent(Content content) {
        set(CONTENT, content);
        return this;
    }

    public Content getContent() {
        return get(CONTENT);
    }

    public TemporaryRecord setTitle(String title) {
        set(TITLE, title);
        return this;
    }

    public int getNumberOfDaysBeforeDestruction(){
        return Integer.parseInt((String) get(DAY_BEFORE_DESTRUCTION));
    }

    public TemporaryRecord setNumberOfDaysBeforeDestruction(int numberOfDaysBeforeDestruction) {
        set(DAY_BEFORE_DESTRUCTION, numberOfDaysBeforeDestruction);
        return this;
    }

    public String getTitle() {
        return get(TITLE);
    }
}

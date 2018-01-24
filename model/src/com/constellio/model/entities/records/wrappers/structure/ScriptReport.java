package com.constellio.model.entities.records.wrappers.structure;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.TemporaryRecord;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

import java.util.ArrayList;
import java.util.List;

public class ScriptReport extends TemporaryRecord {
    public static final String SCHEMA = "scriptReport";
    public static final String FULL_SCHEMA = SCHEMA_TYPE + "_" + SCHEMA;

    public ScriptReport(Record record, MetadataSchemaTypes types) {
        super(record, types);
    }
}

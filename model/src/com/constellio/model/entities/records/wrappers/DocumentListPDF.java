package com.constellio.model.entities.records.wrappers;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

public class DocumentListPDF extends TemporaryRecord
{
    public static final String SCHEMA_NAME = "ConsolidatedPdf";
    public static final String SCHEMA = SCHEMA_TYPE + "_" + SCHEMA_NAME;
    public static final String DEFAULT_SCHEMA = SCHEMA_TYPE + "_default";
    public static final String DEFAULT = "";

    public DocumentListPDF(Record record, MetadataSchemaTypes types) {
        super(record, types);
    }

    public DocumentListPDF(Record record, MetadataSchemaTypes types, String typeRequirement) {
        super(record, types, typeRequirement);
    }

    
}

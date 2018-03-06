package com.constellio.model.entities.records.wrappers;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

public class DocumentListPDF extends TemporaryRecord
{
    public static final String SCHEMA = "ConsolidatedPdf";
    public static final String FULL_SCHEMA = SCHEMA_TYPE + "_" + SCHEMA;

    public DocumentListPDF(Record record, MetadataSchemaTypes types) {
        super(record, types);
    }

    public DocumentListPDF(Record record, MetadataSchemaTypes types, String typeRequirement) {
        super(record, types, typeRequirement);
    }

    
}

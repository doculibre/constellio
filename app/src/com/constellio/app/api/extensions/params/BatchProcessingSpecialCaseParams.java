package com.constellio.app.api.extensions.params;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;

public class BatchProcessingSpecialCaseParams {
    Record record;
    Metadata metadata;

    public BatchProcessingSpecialCaseParams(Record record, Metadata metadata) {
        this.record = record;
        this.metadata = metadata;
    }

    public Record getRecord() {
        return this.record;
    }

    public Metadata getMetadata() {
        return this.metadata;
    }
}

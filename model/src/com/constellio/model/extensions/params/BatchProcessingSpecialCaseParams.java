package com.constellio.model.extensions.params;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;

public class BatchProcessingSpecialCaseParams {
    Record record;

    public BatchProcessingSpecialCaseParams(Record record) {
        this.record = record;
    }


    public Record getRecord() {
        return this.record;
    }
}

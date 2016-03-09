package com.constellio.model.services.records.extractions;

import com.constellio.model.entities.records.Record;

public interface RecordPopulator {
    Object getPopulationValue(Record record);
}

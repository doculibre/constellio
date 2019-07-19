package com.constellio.model.services.records.cache;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;

import java.util.List;

public interface FindMultipleRecordByMetadata {
	List<Record> getMultipleByMetadata(Metadata metadata, String value);
}

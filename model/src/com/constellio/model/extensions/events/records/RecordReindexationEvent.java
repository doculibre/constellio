package com.constellio.model.extensions.events.records;

import com.constellio.model.entities.records.Record;
import com.constellio.model.services.schemas.SchemaUtils;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class RecordReindexationEvent implements RecordEvent {

	Record record;

	@Override
	public Record getRecord() {
		return record;
	}

	public String getSchemaTypeCode() {
		return SchemaUtils.getSchemaTypeCode(record.getSchemaCode());
	}

}

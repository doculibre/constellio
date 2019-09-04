package com.constellio.model.extensions.events.records;

import com.constellio.model.entities.records.Record;
import com.constellio.model.services.schemas.MetadataList;
import com.constellio.model.services.schemas.SchemaUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public abstract class RecordReindexationEvent implements RecordEvent {

	private Record record;
	private MetadataList modifiedMetadatas;

	public String getSchemaTypeCode() {
		return SchemaUtils.getSchemaTypeCode(record.getSchemaCode());
	}

	public abstract void recalculateRecord(List<String> metadatas);

}

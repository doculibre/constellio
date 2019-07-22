package com.constellio.model.services.records;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.services.search.StreamAdaptor;

import java.util.stream.Stream;

public class RecordStream extends StreamAdaptor<Record> {

	public RecordStream(Stream<Record> adapted) {
		super(adapted);
	}

	public RecordStream onlySchemaType(String schemaType) {
		filter(record -> schemaType.equals(record.getTypeCode()));
		return this;
	}

	public RecordStream onlySchemaType(MetadataSchemaType schemaType) {
		return onlySchemaType(schemaType.getCode());
	}

	public RecordStream onlySchema(String schema) {
		filter(record -> schema.equals(record.getSchemaCode()));
		return this;
	}

	public RecordStream onlySchema(MetadataSchema schema) {
		return onlySchema(schema.getCode());
	}
}

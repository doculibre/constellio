package com.constellio.model.entities.records.wrappers;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

public class Conversation extends RecordWrapper {
	public static final String SCHEMA_TYPE = "conversation";

	public Conversation(Record record, MetadataSchemaTypes types) {
		super(record, types, SCHEMA_TYPE);
	}
}
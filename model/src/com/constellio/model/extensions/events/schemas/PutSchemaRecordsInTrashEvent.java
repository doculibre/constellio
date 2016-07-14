package com.constellio.model.extensions.events.schemas;

public class PutSchemaRecordsInTrashEvent implements SchemaEvent {
	String schemaCode;

	public PutSchemaRecordsInTrashEvent(String schemaCode) {
		this.schemaCode = schemaCode;
	}

	@Override
	public String getSchemaCode() {
		return schemaCode;
	}
}

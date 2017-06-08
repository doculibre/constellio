package com.constellio.model.extensions.events.schemas;

import com.constellio.model.entities.records.wrappers.User;

public class PutSchemaRecordsInTrashEvent implements SchemaEvent {
	String schemaCode;
	User user;

	public PutSchemaRecordsInTrashEvent(String schemaCode, User user) {
		this.schemaCode = schemaCode;
		this.user = user;
	}

	@Override
	public String getSchemaCode() {
		return schemaCode;
	}

	@Override
	public User getUser() {
		return user;
	}
}

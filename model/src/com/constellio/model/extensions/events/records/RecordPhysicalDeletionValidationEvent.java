package com.constellio.model.extensions.events.records;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.schemas.SchemaUtils;

public class RecordPhysicalDeletionValidationEvent implements RecordEvent {

	User user;

	Record record;

	public RecordPhysicalDeletionValidationEvent(Record record, User user) {
		this.record = record;
		this.user = user;
	}

	public Record getRecord() {
		return record;
	}

	public User getUser() {
		return user;
	}

	public String getSchemaTypeCode() {
		return new SchemaUtils().getSchemaTypeCode(record.getSchemaCode());
	}
}

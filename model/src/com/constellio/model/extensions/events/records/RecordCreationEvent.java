package com.constellio.model.extensions.events.records;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.schemas.SchemaUtils;

public class RecordCreationEvent implements RecordEvent {

	Record record;
	User transactionalUser;

	public void setRecord(Record record) {
		this.record = record;
	}

	public User getTransactionalUser() {
		return transactionalUser;
	}

	public void setTransactionalUser(User transactionalUser) {
		this.transactionalUser = transactionalUser;
	}

	public RecordCreationEvent(Record record, User transactionalUser) {
		this.record = record;
		this.transactionalUser = transactionalUser;
	}

	public Record getRecord() {
		return record;
	}

	public boolean isSchemaType(String schemaType) {
		return schemaType.equals(getSchemaTypeCode());
	}

	public String getSchemaTypeCode() {
		return new SchemaUtils().getSchemaTypeCode(record.getSchemaCode());
	}
}

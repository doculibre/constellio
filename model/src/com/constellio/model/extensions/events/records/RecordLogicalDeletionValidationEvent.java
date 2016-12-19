package com.constellio.model.extensions.events.records;

import com.constellio.data.utils.Factory;
import com.constellio.data.utils.FactoryWithCache;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.schemas.SchemaUtils;

public class RecordLogicalDeletionValidationEvent implements RecordEvent {

	User user;

	Record record;

	Factory<Boolean> referenceCount;

	public RecordLogicalDeletionValidationEvent(Record record, User user, Factory<Boolean> referenceCount) {
		this.record = record;
		this.user = user;
		this.referenceCount = new FactoryWithCache<>(referenceCount);
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

	public boolean isSchemaType(String schemaType) {
		return schemaType.equals(getSchemaTypeCode());
	}

	public boolean isRecordReferenced() {
		return referenceCount.get();
	}

	public String getCollection() {
		return record.getCollection();
	}
}

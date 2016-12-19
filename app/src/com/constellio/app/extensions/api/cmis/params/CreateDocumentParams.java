package com.constellio.app.extensions.api.cmis.params;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;

public class CreateDocumentParams {

	User user;

	Record record;

	public CreateDocumentParams(User user, Record record) {
		this.user = user;
		this.record = record;
	}

	public boolean isOfType(String type) {
		return record.getTypeCode().equals(type);
	}

	public User getUser() {
		return user;
	}

	public Record getRecord() {
		return record;
	}
}

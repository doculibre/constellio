package com.constellio.model.extensions.params;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;

public class BatchProcessingSpecialCaseParams {
	Record record;
	User user;

	public BatchProcessingSpecialCaseParams(Record record, User user) {
		this.record = record;
		this.user = user;
	}


	public Record getRecord() {
		return this.record;
	}

	public User getUser() {
		return user;
	}
}

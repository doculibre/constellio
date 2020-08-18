package com.constellio.model.extensions.events.schemas;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.records.RecordPhysicalDeleteOptions;

public class PreparePhysicalDeleteFromTrashParams {

	private Record record;

	private User user;

	private RecordPhysicalDeleteOptions deleteOptions;

	public PreparePhysicalDeleteFromTrashParams(Record record, User user, RecordPhysicalDeleteOptions deleteOptions) {
		this.record = record;
		this.user = user;
		this.deleteOptions = deleteOptions;
	}

	public Record getRecord() {
		return record;
	}

	public User getUser() {
		return user;
	}

	public RecordPhysicalDeleteOptions getDeleteOptions() {
		return deleteOptions;
	}

}

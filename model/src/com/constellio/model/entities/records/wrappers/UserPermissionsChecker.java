package com.constellio.model.entities.records.wrappers;

import com.constellio.model.entities.records.Record;

public abstract class UserPermissionsChecker {

	protected User user;

	protected UserPermissionsChecker(User user) {
		this.user = user;
	}

	public abstract boolean globally();

	public abstract boolean on(Record record);

	public abstract boolean onSomething();

	public boolean on(RecordWrapper recordWrapper) {
		return on(recordWrapper.getWrappedRecord());
	}

	public boolean onAll(RecordWrapper... recordWrappers) {
		Record[] records = new Record[recordWrappers.length];
		for (int i = 0; i < recordWrappers.length; i++) {
			records[i] = recordWrappers[i].getWrappedRecord();
		}
		return onAll(records);
	}

	public boolean onAny(RecordWrapper... recordWrappers) {
		Record[] records = new Record[recordWrappers.length];
		for (int i = 0; i < recordWrappers.length; i++) {
			records[i] = recordWrappers[i].getWrappedRecord();
		}
		return onAny(records);
	}

	public boolean onAll(Record... records) {
		if (user.isSystemAdmin()) {
			return true;
		}
		for (Record record : records) {
			if (!on(record)) {
				return false;
			}
		}
		return true;
	}

	public boolean onAny(Record... records) {
		if (user.isSystemAdmin()) {
			return true;
		}
		for (Record record : records) {
			if (on(record)) {
				return true;
			}
		}
		return false;
	}

}

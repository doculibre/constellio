package com.constellio.app.modules.rm.extensions.api.reports;

import com.constellio.model.entities.records.wrappers.RecordWrapper;

public class BaseSingleRecordReportFactoryParams<T extends RecordWrapper> {


	private T wrappedRecord;

	public BaseSingleRecordReportFactoryParams(T wrappedRecord) {
		this.wrappedRecord = wrappedRecord;
	}

	public String getCollection() {
		return wrappedRecord.getCollection();
	}

	public T getWrappedRecord() {
		return wrappedRecord;
	}

}

package com.constellio.app.services.importExport.records;

import com.constellio.model.entities.records.Record;

import java.util.Iterator;

public class RecordExportOptions {

	boolean isForSameSystem = false;

	Iterator<Record> recordsToExportIterator;

	public boolean isForSameSystem() {
		return isForSameSystem;
	}

	public RecordExportOptions setForSameSystem(boolean forSameSystem) {
		isForSameSystem = forSameSystem;
		return this;
	}

	public Iterator<Record> getRecordsToExportIterator() {
		return recordsToExportIterator;
	}

	public RecordExportOptions setRecordsToExportIterator(
			Iterator<Record> recordsToExportIterator) {
		this.recordsToExportIterator = recordsToExportIterator;
		return this;
	}
}

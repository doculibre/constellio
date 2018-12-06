package com.constellio.app.api.extensions.params;

import com.constellio.app.services.importExport.records.writers.ModifiableImportRecord;
import com.constellio.model.entities.records.Record;

public class OnWriteRecordParams {

	Record record;

	ModifiableImportRecord modifiableImportRecord;

	boolean isForSameSystem;

	public OnWriteRecordParams(Record record,
							   ModifiableImportRecord modifiableImportRecord,
							   boolean isForSameSystem) {
		this.record = record;
		this.modifiableImportRecord = modifiableImportRecord;
		this.isForSameSystem = isForSameSystem;
	}

	public Record getRecord() {
		return record;
	}

	public ModifiableImportRecord getModifiableImportRecord() {
		return modifiableImportRecord;
	}

	public boolean isRecordOfType(String schemaType) {
		return record.getTypeCode().equals(schemaType);
	}

	public boolean isForSameSystem() {
		return isForSameSystem;
	}
}

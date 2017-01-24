package com.constellio.app.api.extensions.params;

import com.constellio.app.services.importExport.records.writers.ModifiableImportRecord;
import com.constellio.model.entities.records.Record;

public class OnWriteRecordParams {

	Record record;

	ModifiableImportRecord modifiableImportRecord;

	public OnWriteRecordParams(Record record,
			ModifiableImportRecord modifiableImportRecord) {
		this.record = record;
		this.modifiableImportRecord = modifiableImportRecord;
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
}

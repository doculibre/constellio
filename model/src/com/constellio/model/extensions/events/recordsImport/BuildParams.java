package com.constellio.model.extensions.events.recordsImport;

import com.constellio.app.services.schemas.bulkImport.data.ImportData;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

public class BuildParams {

	Record record;
	MetadataSchemaTypes types;
	ImportData importRecord;

	public BuildParams(Record record, MetadataSchemaTypes types,
			ImportData importRecord) {
		this.record = record;
		this.types = types;
		this.importRecord = importRecord;
	}

	public Record getRecord() {
		return record;
	}

	public MetadataSchemaTypes getTypes() {
		return types;
	}

	public ImportData getImportRecord() {
		return importRecord;
	}
}

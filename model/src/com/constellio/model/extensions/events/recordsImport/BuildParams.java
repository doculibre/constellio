package com.constellio.model.extensions.events.recordsImport;

import com.constellio.app.services.schemas.bulkImport.data.ImportData;
import com.constellio.app.services.schemas.bulkImport.data.ImportDataOptions;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

public class BuildParams {

	Record record;
	MetadataSchemaTypes types;
	ImportData importRecord;
	ImportDataOptions importDataOptions;
	boolean isAllowingReferencesToNonExistingUsers;

	public BuildParams(Record record, MetadataSchemaTypes types,
					   ImportData importRecord, ImportDataOptions importDataOptions, boolean isAllowingReferencesToNonExistingUsers) {
		this.record = record;
		this.types = types;
		this.importRecord = importRecord;
		this.importDataOptions = importDataOptions;
		this.isAllowingReferencesToNonExistingUsers = isAllowingReferencesToNonExistingUsers;
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

	public ImportDataOptions getImportDataOptions() {return this.importDataOptions;}

	public boolean isAllowingReferencesToNonExistingUsers() {
		return isAllowingReferencesToNonExistingUsers;
	}
}

package com.constellio.model.extensions.events.recordsImport;

import com.constellio.app.services.schemas.bulkImport.data.ImportData;
import com.constellio.app.services.schemas.bulkImport.data.ImportDataOptions;
import com.constellio.model.frameworks.validation.ValidationErrors;

public class ValidationParams {

	ValidationErrors errors;
	ImportData importRecord;
	ImportDataOptions options;
	boolean warningsForInvalidFacultativeMetadatas;

	public ValidationParams(ValidationErrors errors, ImportData importRecord, ImportDataOptions options, boolean warningsForInvalidFacultativeMetadatas) {
		this.errors = errors;
		this.importRecord = importRecord;
		this.options = options;
		this.warningsForInvalidFacultativeMetadatas= warningsForInvalidFacultativeMetadatas;
	}

	public ValidationErrors getErrors() {
		return errors;
	}

	public ImportData getImportRecord() {
		return importRecord;
	}

	public ImportDataOptions getOptions() {
		return options;
	}

	public boolean isWarningsForInvalidFacultativeMetadatas() {
		return warningsForInvalidFacultativeMetadatas;
	}
}

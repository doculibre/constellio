package com.constellio.model.extensions.events.recordsImport;

import com.constellio.app.services.schemas.bulkImport.ImportDataErrors;
import com.constellio.app.services.schemas.bulkImport.data.ImportData;
import com.constellio.model.frameworks.validation.ValidationErrors;

public class PrevalidationParams {

	ValidationErrors errors;
	ImportData importRecord;

	public PrevalidationParams(ValidationErrors errors, ImportData importRecord) {
		this.errors = errors;
		this.importRecord = importRecord;
	}

	public ValidationErrors getErrors() {
		return errors;
	}

	public ImportData getImportRecord() {
		return importRecord;
	}
}

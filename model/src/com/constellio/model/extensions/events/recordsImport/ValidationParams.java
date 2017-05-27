package com.constellio.model.extensions.events.recordsImport;

import com.constellio.app.services.schemas.bulkImport.ImportDataErrors;
import com.constellio.app.services.schemas.bulkImport.data.ImportData;
import com.constellio.app.services.schemas.bulkImport.data.ImportDataOptions;
import com.constellio.model.frameworks.validation.ValidationErrors;

public class ValidationParams {

	ValidationErrors errors;
	ImportData importRecord;
	ImportDataOptions options;

	public ValidationParams(ValidationErrors errors, ImportData importRecord, ImportDataOptions options) {
		this.errors = errors;
		this.importRecord = importRecord;
		this.options = options;
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
}

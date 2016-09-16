package com.constellio.app.services.schemas.bulkImport;

public class BulkImportParams {

	ImportErrorsBehavior importErrorsBehavior = ImportErrorsBehavior.STOP_ON_FIRST_ERROR;

	ImportValidationErrorsBehavior importValidationErrorsBehavior = ImportValidationErrorsBehavior.STOP_IMPORT;

	int batchSize = 100;

	public int getBatchSize() {
		return batchSize;
	}

	public BulkImportParams setBatchSize(int batchSize) {
		this.batchSize = batchSize;
		return this;
	}

	public ImportErrorsBehavior getImportErrorsBehavior() {
		return importErrorsBehavior;
	}

	public BulkImportParams setImportErrorsBehavior(ImportErrorsBehavior importErrorsBehavior) {
		this.importErrorsBehavior = importErrorsBehavior;
		return this;
	}

	public ImportValidationErrorsBehavior getImportValidationErrorsBehavior() {
		return importValidationErrorsBehavior;
	}

	public BulkImportParams setImportValidationErrorsBehavior(ImportValidationErrorsBehavior importValidationErrorsBehavior) {
		this.importValidationErrorsBehavior = importValidationErrorsBehavior;
		return this;
	}

	public static enum ImportValidationErrorsBehavior {STOP_IMPORT, EXCLUDE_THOSE_RECORDS}

	public static enum ImportErrorsBehavior {
		STOP_ON_FIRST_ERROR,
		CONTINUE_FOR_RECORD_OF_SAME_TYPE,
		CONTINUE
	}
}

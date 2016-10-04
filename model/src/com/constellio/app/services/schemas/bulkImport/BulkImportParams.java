package com.constellio.app.services.schemas.bulkImport;

import static com.constellio.app.services.schemas.bulkImport.BulkImportParams.ImportErrorsBehavior.CONTINUE;

public class BulkImportParams {

	boolean warningsForInvalidFacultativeMetadatas = false;

	ImportErrorsBehavior importErrorsBehavior = ImportErrorsBehavior.STOP_ON_FIRST_ERROR;

	ImportValidationErrorsBehavior importValidationErrorsBehavior = ImportValidationErrorsBehavior.STOP_IMPORT;

	int batchSize = 500;

	int threads = 1;

	public int getThreads() {
		return threads;
	}

	public BulkImportParams setThreads(int threads) {
		this.threads = threads;
		return this;
	}

	public int getBatchSize() {
		return batchSize;
	}

	public BulkImportParams setBatchSize(int batchSize) {
		this.batchSize = batchSize;
		return this;
	}

	public boolean isWarningsForInvalidFacultativeMetadatas() {
		return warningsForInvalidFacultativeMetadatas;
	}

	public BulkImportParams setWarningsForInvalidFacultativeMetadatas(boolean warningsForInvalidFacultativeMetadatas) {
		this.warningsForInvalidFacultativeMetadatas = warningsForInvalidFacultativeMetadatas;
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

	public static BulkImportParams IMPORT_AS_MUCH_AS_POSSIBLE() {
		return new BulkImportParams().setWarningsForInvalidFacultativeMetadatas(true).setImportErrorsBehavior(CONTINUE)
				.setImportValidationErrorsBehavior(ImportValidationErrorsBehavior.EXCLUDE_THOSE_RECORDS);
	}

	public static BulkImportParams STRICT() {
		return new BulkImportParams();
	}
}

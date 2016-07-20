package com.constellio.app.services.schemas.bulkImport;

public class BulkImportParams {

	boolean stopOnFirstError = true;

	public boolean isStopOnFirstError() {
		return stopOnFirstError;
	}

	public BulkImportParams setStopOnFirstError(boolean stopOnFirstError) {
		this.stopOnFirstError = stopOnFirstError;
		return this;
	}
}

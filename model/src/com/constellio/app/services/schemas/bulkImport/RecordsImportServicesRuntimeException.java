package com.constellio.app.services.schemas.bulkImport;

public class RecordsImportServicesRuntimeException extends RuntimeException {

	public RecordsImportServicesRuntimeException(Throwable t) {
		super(t);
	}

	public RecordsImportServicesRuntimeException(String message) {
		super(message);
	}

	public RecordsImportServicesRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

}

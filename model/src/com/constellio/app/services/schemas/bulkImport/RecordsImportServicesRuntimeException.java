package com.constellio.app.services.schemas.bulkImport;

import java.util.List;

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

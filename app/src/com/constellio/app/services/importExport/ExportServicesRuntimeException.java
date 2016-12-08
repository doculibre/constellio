package com.constellio.app.services.importExport;

public class ExportServicesRuntimeException extends RuntimeException {

	public ExportServicesRuntimeException(String message) {
		super(message);
	}

	public ExportServicesRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public ExportServicesRuntimeException(Throwable cause) {
		super(cause);
	}

	public static class ExportServicesRuntimeException_FailedToZip extends ExportServicesRuntimeException {

		public ExportServicesRuntimeException_FailedToZip(String collection, Throwable cause) {
			super("Cannot zip records and settings of collection '" + collection + "'", cause);
		}

		public ExportServicesRuntimeException_FailedToZip(Throwable cause) {
			super("Cannot zip records and settings of collection", cause);
		}
	}
}

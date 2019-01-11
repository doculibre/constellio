package com.constellio.app.services.importExport.records;

public class RecordExportServicesRuntimeException extends RuntimeException {

	public RecordExportServicesRuntimeException(String message) {
		super(message);
	}

	public RecordExportServicesRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public RecordExportServicesRuntimeException(Throwable cause) {
		super(cause);
	}

	public static class ExportServicesRuntimeException_FailedToZip extends RecordExportServicesRuntimeException {

		public ExportServicesRuntimeException_FailedToZip(Throwable cause) {
			super("Cannot zip records ", cause);
		}

	}

	public static class ExportServicesRuntimeException_NoRecords extends RecordExportServicesRuntimeException {

		public ExportServicesRuntimeException_NoRecords() {
			super("No records");
		}
	}
}

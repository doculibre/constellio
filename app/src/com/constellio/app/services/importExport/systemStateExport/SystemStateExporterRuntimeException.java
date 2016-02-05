package com.constellio.app.services.importExport.systemStateExport;

public class SystemStateExporterRuntimeException extends RuntimeException {

	public SystemStateExporterRuntimeException(String message) {
		super(message);
	}

	public SystemStateExporterRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public SystemStateExporterRuntimeException(Throwable cause) {
		super(cause);
	}

	public static class SystemStateExporterRuntimeException_InvalidRecordId extends SystemStateExporterRuntimeException {
		public SystemStateExporterRuntimeException_InvalidRecordId(String recordId) {
			super("Invalid record id '" + recordId + "'");
		}
	}

	public static class SystemStateExporterRuntimeException_RecordHasNoContent extends SystemStateExporterRuntimeException {
		public SystemStateExporterRuntimeException_RecordHasNoContent(String recordId) {
			super("Record with id '" + recordId + "' has no content");
		}
	}
}

package com.constellio.app.modules.es.services;

public class ESSchemaRecordsServicesRuntimeException extends RuntimeException {

	public ESSchemaRecordsServicesRuntimeException(String message) {
		super(message);
	}

	public ESSchemaRecordsServicesRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public ESSchemaRecordsServicesRuntimeException(Throwable cause) {
		super(cause);
	}

	public static class ESSchemaRecordsServicesRuntimeException_RecordIsNotAConnectorDocument
			extends ESSchemaRecordsServicesRuntimeException {
		public ESSchemaRecordsServicesRuntimeException_RecordIsNotAConnectorDocument(String schemaCode) {
			super("Record with schema '" + schemaCode + "' is not a connector document");
		}
	}
}

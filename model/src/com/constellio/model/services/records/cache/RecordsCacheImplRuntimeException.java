package com.constellio.model.services.records.cache;

public class RecordsCacheImplRuntimeException extends RuntimeException {

	public RecordsCacheImplRuntimeException(String message) {
		super(message);
	}

	public RecordsCacheImplRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public RecordsCacheImplRuntimeException(Throwable cause) {
		super(cause);
	}

	public static class RecordsCacheImplRuntimeException_CacheAlreadyConfigured extends RecordsCacheImplRuntimeException {

		public RecordsCacheImplRuntimeException_CacheAlreadyConfigured(String schemaType) {
			super("There is already a cache configured with schema type '" + schemaType + "'");
		}
	}

	public static class RecordsCacheImplRuntimeException_InvalidSchemaTypeCode extends RecordsCacheImplRuntimeException {

		public RecordsCacheImplRuntimeException_InvalidSchemaTypeCode(String schemaType) {
			super("Schema type '" + schemaType + "' is invalid");
		}
	}
}

package com.constellio.model.services.schemas;

public class SchemaUtilsRuntimeException extends RuntimeException {

	public SchemaUtilsRuntimeException(String message) {
		super(message);
	}

	public SchemaUtilsRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public SchemaUtilsRuntimeException(Throwable cause) {
		super(cause);
	}

	public static final class SchemaUtilsRuntimeException_NoMetadataWithDatastoreCode extends SchemaUtilsRuntimeException {
		public SchemaUtilsRuntimeException_NoMetadataWithDatastoreCode(String dataStoreCode) {
			super("No metadata with datastore code '" + dataStoreCode + "'");
		}
	}
}

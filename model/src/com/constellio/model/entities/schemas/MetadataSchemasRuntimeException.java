package com.constellio.model.entities.schemas;

@SuppressWarnings("serial")
public class MetadataSchemasRuntimeException extends RuntimeException {

	public MetadataSchemasRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public MetadataSchemasRuntimeException(String message) {
		super(message);
	}

	public static class NoSuchMetadata extends MetadataSchemasRuntimeException {

		public NoSuchMetadata(String code) {
			super("The metadata code : '" + code + "' doesn't exist!");
		}
	}

	public static class NoSuchSchema extends MetadataSchemasRuntimeException {

		public NoSuchSchema(String code) {
			super("The schema code : '" + code + "' doesn't exist!");
		}

		public NoSuchSchema(short id) {
			super("The schema id : '" + id + "' doesn't exist!");
		}
	}

	public static class CannotGetMetadatasOfAnotherSchema extends MetadataSchemasRuntimeException {
		public CannotGetMetadatasOfAnotherSchema(String otherSchemaCode, String recordSchemaCode) {
			super("Cannot get metadata of schema '" + otherSchemaCode + "' in a record of schema '" + recordSchemaCode + "'");
		}
	}

	public static class CannotGetMetadatasOfAnotherSchemaType extends MetadataSchemasRuntimeException {
		public CannotGetMetadatasOfAnotherSchemaType(String otherSchemaTypeCode, String recordSchemaTypeCode) {
			super("Cannot get metadata of schema type '" + otherSchemaTypeCode + "' in a record of schema '"
				  + recordSchemaTypeCode + "'");
		}
	}

	public static class InvalidCode extends MetadataSchemasRuntimeException {
		public InvalidCode(String code) {
			super("Invalid code : " + code);
		}
	}

	public static class InvalidCodeFormat extends MetadataSchemasRuntimeException {
		public InvalidCodeFormat(String code) {
			super("Invalid code format : " + code);
		}
	}

	public static class NoSuchSchemaType extends MetadataSchemasRuntimeException {

		public NoSuchSchemaType(String code) {
			super("No such schema type '" + code + "'");
		}

		public NoSuchSchemaType(short id) {
			super("No such schema type with id '" + id + "'");
		}
	}

	public static class NoSuchMetadataWithAtomicCode extends MetadataSchemasRuntimeException {

		public NoSuchMetadataWithAtomicCode(String code, Exception e) {
			super("No such metadata with local code '" + code + "'", e);
		}
	}

	public static class NoSuchMetadataWithDatastoreCodeInSchemaType extends MetadataSchemasRuntimeException {

		public NoSuchMetadataWithDatastoreCodeInSchemaType(String dataStoreCode, String schemaTypeCode) {
			super("No metadata with datastore code " + dataStoreCode + " in  schema type " + schemaTypeCode);
		}
	}

}

package com.constellio.model.services.schemas.builders;

@SuppressWarnings("serial")
public class MetadataSchemaTypeBuilderRuntimeException extends RuntimeException {

	public MetadataSchemaTypeBuilderRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public MetadataSchemaTypeBuilderRuntimeException(String message) {
		super(message);
	}

	public MetadataSchemaTypeBuilderRuntimeException(Throwable cause) {
		super(cause);
	}

	public static class NoSuchSchema extends MetadataSchemaTypeBuilderRuntimeException {
		public NoSuchSchema(String schema) {
			super("No such schema : '" + schema + "'");
		}
	}

	public static class SchemaAlreadyDefined extends MetadataSchemaTypeBuilderRuntimeException {
		public SchemaAlreadyDefined(String schema) {
			super("Schema '" + schema + "' is already defined");
		}
	}

	public static class CodeCannotBeModified extends MetadataSchemaTypeBuilderRuntimeException {
		public CodeCannotBeModified(String schema) {
			super("The code of a schema cannot be modified after it has been saved");
		}
	}

	public static class CannotDeleteSchemaType extends MetadataSchemaTypeBuilderRuntimeException {
		public CannotDeleteSchemaType(String code) {
			super("The schema type '" + code + "' is undeletable!");
		}
	}

	public static class LabelNotDefined extends MetadataSchemaTypeBuilderRuntimeException {
		public LabelNotDefined(String code) {
			super("The schema type '" + code + "' has no label");
		}
	}

}

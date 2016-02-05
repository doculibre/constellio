package com.constellio.model.services.schemas.builders;

@SuppressWarnings("serial")
public class AllowedReferencesBuilderRuntimeException extends RuntimeException {

	private AllowedReferencesBuilderRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	private AllowedReferencesBuilderRuntimeException(String message) {
		super(message);
	}

	private AllowedReferencesBuilderRuntimeException(Throwable cause) {
		super(cause);
	}

	public static class SchemaTypeAlreadySet extends AllowedReferencesBuilderRuntimeException {
		public SchemaTypeAlreadySet() {
			super("This AllowedReferences already has an allowed type.");
		}
	}

	public static class CannotHaveBothATypeAndSchemas extends AllowedReferencesBuilderRuntimeException {
		public CannotHaveBothATypeAndSchemas() {
			super("AllowedReferences can have either a type or schemas, never both.");
		}
	}

	public static class AllSchemasMustBeOfSameType extends AllowedReferencesBuilderRuntimeException {
		public AllSchemasMustBeOfSameType() {
			super("All schemas in AllowedReferences cmust be of the same type.");
		}
	}

}

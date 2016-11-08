package com.constellio.model.services.schemas.builders;

public class DataEntryBuilderRuntimeException extends RuntimeException {

	public DataEntryBuilderRuntimeException(String message) {
		super(message);
	}

	public static class DataEntryBuilderRuntimeException_InvalidMetadataCode extends DataEntryBuilderRuntimeException {

		public DataEntryBuilderRuntimeException_InvalidMetadataCode(String argument, String metadataCode) {
			super("Metadata code '" + metadataCode + "' of argument '" + argument
					+ "' is invalid. It must be a complete of a metadata in a default schema");
		}
	}

	public static class DataEntryBuilderRuntimeException_MetadatasMustBeOfSameSchemaType
			extends DataEntryBuilderRuntimeException {

		public DataEntryBuilderRuntimeException_MetadatasMustBeOfSameSchemaType(String numberCode, String refCode) {
			super("Metadatas must be of same schema type : " + numberCode + "/" + refCode);
		}
	}

	public static class DataEntryBuilderRuntimeException_AgregatedMetadatasNotSupportedOnCustomSchemas
			extends DataEntryBuilderRuntimeException {

		public DataEntryBuilderRuntimeException_AgregatedMetadatasNotSupportedOnCustomSchemas() {
			super("Agregated metadatas are not supported on metadatas of custom schemas");
		}
	}
}

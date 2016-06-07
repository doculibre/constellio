package com.constellio.model.services.schemas.builders;

import com.constellio.model.entities.schemas.MetadataValueType;

@SuppressWarnings("serial")
public class MetadataSchemaTypesBuilderRuntimeException extends RuntimeException {

	public MetadataSchemaTypesBuilderRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public MetadataSchemaTypesBuilderRuntimeException(String message) {
		super(message);
	}

	public static class SchemaTypeExistent extends MetadataSchemaTypesBuilderRuntimeException {
		public SchemaTypeExistent(String code) {
			super("The schema code : '" + code + "' already exists!");
		}
	}

	public static class NoSuchSchemaType extends MetadataSchemaTypesBuilderRuntimeException {
		public NoSuchSchemaType(String code) {
			super("The schema type code : '" + code + "' doesn't exist!");
		}
	}

	public static class NoSuchSchema extends MetadataSchemaTypesBuilderRuntimeException {
		public NoSuchSchema(String code) {
			super("The schema code : '" + code + "' doesn't exist!");
		}
	}

	public static class NoSuchMetadata extends MetadataSchemaTypesBuilderRuntimeException {
		public NoSuchMetadata(String code) {
			super("The metadata code : '" + code + "' doesn't exist!");
		}
	}

	public static class InvalidCodeFormat extends MetadataSchemaTypesBuilderRuntimeException {
		public InvalidCodeFormat(Exception e, String code) {
			super("Invalid format code : '" + code, e);
		}
	}

	public static class CannotDeleteSchemaTypeSinceItHasRecords extends MetadataSchemaTypesBuilderRuntimeException {

		public CannotDeleteSchemaTypeSinceItHasRecords(String schemaType) {
			super("Cannot delete schema type since it has records : " + schemaType);
		}
	}

	public static class CannotDeleteSchemaSinceItHasRecords extends MetadataSchemaTypesBuilderRuntimeException {

		public CannotDeleteSchemaSinceItHasRecords(String schema) {
			super("Cannot delete schema since it has records : " + schema);
		}
	}

	public static class CannotCopyMultiValueInSingleValueMetadata extends MetadataSchemaTypesBuilderRuntimeException {
		public CannotCopyMultiValueInSingleValueMetadata(String metadata, String referenceMetadata, String copiedMetadata) {
			super("Cannot copy a multi value metadata '" + copiedMetadata + "' or a multi value reference '" + referenceMetadata
					+ "' in a single value metadata '" + metadata + "'");
		}
	}

	public static class CannotCopySingleValueInMultiValueMetadata extends MetadataSchemaTypesBuilderRuntimeException {
		public CannotCopySingleValueInMultiValueMetadata(String metadata, String referenceMetadata, String copiedMetadata) {
			super("Cannot copy a single value metadata '" + copiedMetadata + "' and a single value reference '"
					+ referenceMetadata + "' in a multi value metadata '" + metadata + "'");
		}
	}

	public static class CannotCopyADifferentTypeInMetadata extends MetadataSchemaTypesBuilderRuntimeException {
		public CannotCopyADifferentTypeInMetadata(String metadata, String typeMetadata, String copiedMetadata,
				String typeCopiedMetadata) {
			super("Cannot copy the value of metadata '" + copiedMetadata + "' with type '" + typeCopiedMetadata
					+ "' in the metadata '" + metadata + "' with type '" + typeMetadata + "'");
		}
	}

	public static class CannotCopyACustomMetadata extends MetadataSchemaTypesBuilderRuntimeException {
		public CannotCopyACustomMetadata(String copiedMetadata) {
			super("Cannot copy the value of '" + copiedMetadata
					+ "' because it is a custom metadata. Refer to a default metadata.");
		}
	}

	public static class CannotCopyUsingACustomMetadata extends MetadataSchemaTypesBuilderRuntimeException {
		public CannotCopyUsingACustomMetadata(String referenceMetadata, String schema) {
			super("Cannot copy a value in '" + referenceMetadata
					+ "' using schema '" + schema + "'. Refer to the default metadata.");
		}
	}

	public static class CannotUseACustomMetadataForCalculation extends MetadataSchemaTypesBuilderRuntimeException {
		public CannotUseACustomMetadataForCalculation(String dependencyMetadata) {
			super("Cannot use the value of '" + dependencyMetadata
					+ "' for calculation because it is a custom metadata. Refer to a default metadata.");
		}
	}

	public static class CyclicDependenciesInSchemas extends MetadataSchemaTypesBuilderRuntimeException {
		public CyclicDependenciesInSchemas(Exception e) {
			super("There is a cyclic dependencies between schemas", e);
		}
	}

	public static class CannotCalculateDifferentValueTypeInValueMetadata extends MetadataSchemaTypesBuilderRuntimeException {
		public CannotCalculateDifferentValueTypeInValueMetadata(String calculatedMetadata, MetadataValueType expectedType,
				MetadataValueType wasType) {
			super("Calculator of '" + calculatedMetadata + "' was expected to calculate a value of type '" + expectedType
					+ "' but is '" + wasType + "'");
		}
	}

	public static class CalculatorDependencyHasInvalidValueType extends MetadataSchemaTypesBuilderRuntimeException {
		public CalculatorDependencyHasInvalidValueType(String calculatedMetadata, String dependencyMetadata,
				MetadataValueType expectedType,
				MetadataValueType wasType) {
			super("Calculator of '" + calculatedMetadata + "' has an invalid dependency on metadata '" + dependencyMetadata
					+ "': Expected type was '" + expectedType + "' but is '" + wasType + "'");
		}
	}

	public static class NoAllowedReferences extends MetadataSchemaTypesBuilderRuntimeException {
		public NoAllowedReferences(String metadata) {
			super("No allowed references for metadata with code : " + metadata);
		}
	}

	public static class NoDependenciesInCalculator extends MetadataSchemaTypesBuilderRuntimeException {
		public NoDependenciesInCalculator(String calculator) {
			super("No dependencies in calculator: " + calculator);
		}
	}

	public static class InvalidDependencyMetadata extends MetadataSchemaTypesBuilderRuntimeException {
		public InvalidDependencyMetadata(String metadataCode, Exception e) {
			super("Invalid metadata: " + metadataCode, e);
		}
	}

	public static class CannotCalculateASingleValueInAMultiValueMetadata extends MetadataSchemaTypesBuilderRuntimeException {
		public CannotCalculateASingleValueInAMultiValueMetadata(String metadataCode, String calculator) {
			super("Cannot calculate a single value in a multi value metadata " + metadataCode + ". Single value calculator : "
					+ calculator);
		}
	}

	public static class CannotCalculateAMultiValueInASingleValueMetadata extends MetadataSchemaTypesBuilderRuntimeException {
		public CannotCalculateAMultiValueInASingleValueMetadata(String metadataCode, String calculator) {
			super("Cannot calculate a multi value in a single value metadata " + metadataCode + ". Multi value calculator : "
					+ calculator);
		}
	}

}

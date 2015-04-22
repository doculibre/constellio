/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
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
		public CannotCalculateDifferentValueTypeInValueMetadata(String metadata, MetadataValueType metadataValueType,
				String calculatedMetadataValueType) {
			super("Cannot calculate different value type " + calculatedMetadataValueType + " in value metadata " + metadata
					+ " with type " + metadataValueType);
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

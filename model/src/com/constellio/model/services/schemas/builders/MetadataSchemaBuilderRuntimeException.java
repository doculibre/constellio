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

import java.util.Collections;
import java.util.List;

import com.constellio.model.utils.DependencyUtilsRuntimeException;

@SuppressWarnings("serial")
public class MetadataSchemaBuilderRuntimeException extends RuntimeException {

	public MetadataSchemaBuilderRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public MetadataSchemaBuilderRuntimeException(String message) {
		super(message);
	}

	public MetadataSchemaBuilderRuntimeException(Throwable cause) {
		super(cause);
	}

	public static class NoSuchMetadata extends MetadataSchemaBuilderRuntimeException {
		public NoSuchMetadata(String codeMetadata) {
			super("No such metadata : '" + codeMetadata + "'");
		}
	}

	public static class MetadataAlreadyExists extends MetadataSchemaBuilderRuntimeException {
		public MetadataAlreadyExists(String codeMetadata) {
			super("Metadata with code '" + codeMetadata + "' already exists");
		}
	}

	public static class CannotDeleteSchema extends MetadataSchemaBuilderRuntimeException {
		public CannotDeleteSchema(String code) {
			super("SchemaType: " + code + " is undeletable");
		}
	}

	public static class CyclicDependenciesInMetadata extends MetadataSchemaBuilderRuntimeException {

		List<String> metadataCodesWithCyclicDependency;

		public CyclicDependenciesInMetadata(DependencyUtilsRuntimeException.CyclicDependency c) {
			super(c.getMessage(), c);
			this.metadataCodesWithCyclicDependency = c.getCyclicDependencies();
		}

		public List<String> getMetadataCodesWithCyclicDependency() {
			return Collections.unmodifiableList(metadataCodesWithCyclicDependency);
		}
	}

	public static class CannotModifyAttributeOfInheritingMetadata extends MetadataSchemaBuilderRuntimeException {
		public CannotModifyAttributeOfInheritingMetadata(String metadataCode, String attribute) {
			super("Cannot modify '" + attribute + "' for metadata '" + metadataCode + "'");
		}
	}

	public static class InvalidAttribute extends MetadataSchemaBuilderRuntimeException {
		public InvalidAttribute(String attribute) {
			super("Invalid " + attribute + "'");
		}
	}

	public static class CannotCreateTwoMetadataWithSameNameInDifferentCustomSchemasOfTheSameType
			extends MetadataSchemaBuilderRuntimeException {
		public CannotCreateTwoMetadataWithSameNameInDifferentCustomSchemasOfTheSameType(String metadataName) {
			super("Cannot create metadata '" + metadataName
					+ "', since a metadata with this name already exist in an other schema");
		}
	}
}

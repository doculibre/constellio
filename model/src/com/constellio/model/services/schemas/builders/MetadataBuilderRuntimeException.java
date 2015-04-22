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

@SuppressWarnings("serial")
public class MetadataBuilderRuntimeException extends RuntimeException {

	public MetadataBuilderRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public MetadataBuilderRuntimeException(String message) {
		super(message);
	}

	public MetadataBuilderRuntimeException(Throwable cause) {
		super(cause);
	}

	public static class MetadataCannotBeUniqueAndMultivalue extends MetadataSchemaBuilderRuntimeException {
		public MetadataCannotBeUniqueAndMultivalue(String code) {
			super("Metadata '" + code + " cannot be unique and multivalue");
		}
	}

	public static class InvalidAttribute extends MetadataBuilderRuntimeException {
		public InvalidAttribute(String metadata, String attribute) {
			super("Invalid attribute '" + attribute + "' on metadata '" + metadata + "'");
		}

		public InvalidAttribute(String metadata, String attribute, Exception e) {
			super("Invalid attribute '" + attribute + "' on metadata '" + metadata + "'", e);
		}
	}

	public static class AllowedReferencesOnlyUsableOnReferenceTypeMetadata extends MetadataBuilderRuntimeException {
		public AllowedReferencesOnlyUsableOnReferenceTypeMetadata(String metadata) {
			super("Cannot set allowed references to metadata '" + metadata + "', since it doesn't have a reference type");
		}
	}

	public static class CannotCreateMultivalueReferenceToPrincipalTaxonomy extends MetadataBuilderRuntimeException {
		public CannotCreateMultivalueReferenceToPrincipalTaxonomy(String metadata) {
			super("Cannot create multivalue reference to principal taxonomy. Metadata '" + metadata);
		}
	}

	public static class ClassNotFound extends MetadataBuilderRuntimeException {
		public ClassNotFound(String className) {
			super("Class not found : '" + className + "'");
		}
	}

	public static class CannotInstanciateClass extends MetadataBuilderRuntimeException {
		public CannotInstanciateClass(String className, Exception e) {
			super("Can not instanciate class: '" + className + "'", e);
		}
	}

	public static class InvalidClass extends MetadataBuilderRuntimeException {
		public InvalidClass(String interfaceName, Exception e) {
			super("Class doesn't implement the interface: '" + interfaceName + "'", e);
		}
	}

	public static class EnumClassMustImplementEnumWithSmallCode extends MetadataBuilderRuntimeException {

		public EnumClassMustImplementEnumWithSmallCode(Class<? extends Enum<?>> enumClass) {
			super("Enum class '" + enumClass + "'  must implement EnumWithSmallCode");
		}
	}
}

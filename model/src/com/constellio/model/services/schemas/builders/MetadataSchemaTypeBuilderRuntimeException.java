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
			super("The schema type '" + code + "' as no label");
		}
	}

}

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

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

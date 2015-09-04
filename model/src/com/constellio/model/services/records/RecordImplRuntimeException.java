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
package com.constellio.model.services.records;

public class RecordImplRuntimeException extends RuntimeException {

	public RecordImplRuntimeException() {
	}

	public RecordImplRuntimeException(String message) {
		super(message);
	}

	public RecordImplRuntimeException(Throwable cause) {
		super(cause);
	}

	public RecordImplRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public static class CannotGetListForSingleValue extends RecordImplRuntimeException {

		public CannotGetListForSingleValue(String metadataCode) {
			super("Cannot getList for the single value metadata: " + metadataCode);
		}
	}

	public static class RecordImplException_RecordCannotHaveTwoParents extends RecordImplRuntimeException {

		public RecordImplException_RecordCannotHaveTwoParents(String recordId, Exception e) {
			super("Record cannot have two parents recordId:" + recordId, e);
		}

		public RecordImplException_RecordCannotHaveTwoParents(String recordId) {
			this(recordId, null);
		}
	}

	public static class RecordImplException_UnsupportedOperationOnUnsavedRecord extends RecordImplRuntimeException {

		public RecordImplException_UnsupportedOperationOnUnsavedRecord(String recordId, String operation) {
			super("Operation '" + operation + "' is unsupported for unsaved recird '" + recordId + "'");
		}
	}

	public static class RecordImplException_CannotChangeSchemaOfSavedRecord extends RecordImplRuntimeException {

		public RecordImplException_CannotChangeSchemaOfSavedRecord(String recordId) {
			super("Cannot change schema of saved record : " + recordId);
		}
	}

	public static class RecordImplException_CannotChangeTypeOfRecord extends RecordImplRuntimeException {

		public RecordImplException_CannotChangeTypeOfRecord(String recordId) {
			super("Cannot change type of record : " + recordId);
		}
	}

	public static class RecordImplException_CannotBuildStructureValue extends RecordImplRuntimeException {

		public RecordImplException_CannotBuildStructureValue(String recordId, String structureValue, Throwable t) {
			super("Cannot build structure value '" + structureValue + "' in record '" + recordId + "'", t);
		}
	}

	public static class RecordImplException_PopulatorReturnedNullValue extends RecordImplRuntimeException {

		public RecordImplException_PopulatorReturnedNullValue(FieldsPopulator populator, String field) {
			super("Populator '" + populator.getClass() + "' returned a null value in field '" + field + "'");
		}
	}
}

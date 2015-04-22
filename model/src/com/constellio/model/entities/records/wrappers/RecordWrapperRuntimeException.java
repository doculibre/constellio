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
package com.constellio.model.entities.records.wrappers;

public class RecordWrapperRuntimeException extends RuntimeException {

	public RecordWrapperRuntimeException(String message) {
		super(message);
	}

	public RecordWrapperRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public RecordWrapperRuntimeException(Throwable cause) {
		super(cause);
	}

	public static class WrappedRecordMustBeNotNull extends RecordWrapperRuntimeException {

		public WrappedRecordMustBeNotNull() {
			super("Wrapped record must be not null");
		}
	}

	public static class MetadataSchemaTypesMustBeNotNull extends RecordWrapperRuntimeException {

		public MetadataSchemaTypesMustBeNotNull() {
			super("Metadata schema types must be not null");
		}
	}

	public static class WrappedRecordMustMeetRequirements extends RecordWrapperRuntimeException {
		public WrappedRecordMustMeetRequirements(String currentType, String typeRequirement) {
			super("Wrapped record type must start with '" + typeRequirement + "'. Was '" + currentType + "'");
		}
	}

	public static class RecordWrapperRuntimeException_CannotUseDisconnectedRecordWrapper extends RecordWrapperRuntimeException {
		public RecordWrapperRuntimeException_CannotUseDisconnectedRecordWrapper() {
			super("Cannot call getter/setter on a disconnected record wrapper.");
		}
	}
}

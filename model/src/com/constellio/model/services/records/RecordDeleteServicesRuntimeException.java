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

@SuppressWarnings("serial")
public class RecordDeleteServicesRuntimeException extends RuntimeException {

	public RecordDeleteServicesRuntimeException(String message) {
		super(message);
	}

	public RecordDeleteServicesRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public RecordDeleteServicesRuntimeException(Throwable cause) {
		super(cause);
	}

	public static class RecordDeleteServicesRuntimeException_CannotDeleteRecordWithUserFromOtherCollection
			extends RecordDeleteServicesRuntimeException {

		public RecordDeleteServicesRuntimeException_CannotDeleteRecordWithUserFromOtherCollection(String recordCollection,
				String userCollection) {
			super("Record of collection '" + recordCollection + "' cannot be deleted with user in colllection '" + userCollection
					+ "'");
		}
	}
	
	public static class RecordDeleteServicesRuntimeException_RecordServicesErrorDuringOperation extends RecordDeleteServicesRuntimeException {
		
		public RecordDeleteServicesRuntimeException_RecordServicesErrorDuringOperation(String operation, Throwable cause) {
			super("RecordServicesException on operation '" + operation + "'", cause);
		}
	}
}

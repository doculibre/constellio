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
package com.constellio.app.services.importExport.systemStateExport;

public class SystemStateExporterRuntimeException extends RuntimeException {

	public SystemStateExporterRuntimeException(String message) {
		super(message);
	}

	public SystemStateExporterRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public SystemStateExporterRuntimeException(Throwable cause) {
		super(cause);
	}

	public static class SystemStateExporterRuntimeException_InvalidRecordId extends SystemStateExporterRuntimeException {
		public SystemStateExporterRuntimeException_InvalidRecordId(String recordId) {
			super("Invalid record id '" + recordId + "'");
		}
	}

	public static class SystemStateExporterRuntimeException_RecordHasNoContent extends SystemStateExporterRuntimeException {
		public SystemStateExporterRuntimeException_RecordHasNoContent(String recordId) {
			super("Record with id '" + recordId + "' has no content");
		}
	}
}

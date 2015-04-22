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
package com.constellio.data.dao.services.bigVault;

@SuppressWarnings("serial")
public class RecordDaoRuntimeException extends RuntimeException {

	public RecordDaoRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public RecordDaoRuntimeException(String message) {
		super(message);
	}

	public RecordDaoRuntimeException(Throwable cause) {
		super(cause);
	}

	public static class RecordDaoRuntimeException_RecordsFlushingFailed extends RecordDaoRuntimeException {

		public RecordDaoRuntimeException_RecordsFlushingFailed(Exception e) {
			super("Records flushing failed", e);
		}

	}

	public static class ReferenceToNonExistentIndex extends RecordDaoRuntimeException {

		private final String id;

		public ReferenceToNonExistentIndex(String id) {
			super("The record cannot be saved, since it references the non-existent index '" + id + "'");
			this.id = id;
		}

		public String getId() {
			return id;
		}
	}

}

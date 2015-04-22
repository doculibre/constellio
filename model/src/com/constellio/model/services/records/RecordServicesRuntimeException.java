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
public class RecordServicesRuntimeException extends RuntimeException {

	public RecordServicesRuntimeException() {
	}

	public RecordServicesRuntimeException(String message) {
		super(message);
	}

	public RecordServicesRuntimeException(Throwable cause) {
		super(cause);
	}

	public RecordServicesRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public static class CannotAddRecord extends RecordServicesRuntimeException {

		public CannotAddRecord(String recordId, Exception e) {
			super("Cannot add record. Record id : " + recordId + ". Other changes may have been committed.", e);
		}

		public CannotAddRecord(String recordId) {
			this(recordId, null);
		}
	}

	public static class NoSuchRecordWithId extends RecordServicesRuntimeException {

		public NoSuchRecordWithId(String id, Exception e) {
			super("No such record with id : " + id, e);
		}
	}

	public static class UserCannotReadDocument extends RecordServicesRuntimeException {

		public UserCannotReadDocument(String id, String username) {
			super("User " + username + " is not authorized to read document " + id + ".");
		}
	}

	public static class RecordIsNotAPrincipalConcept extends RecordServicesRuntimeException {

		public RecordIsNotAPrincipalConcept(String id) {
			super("Record is not a principal concept : " + id);
		}
	}

	public static class NewReferenceToOtherLogicallyDeletedRecord extends RecordServicesRuntimeException {

		public NewReferenceToOtherLogicallyDeletedRecord(String id, Exception e) {
			super("Record cannot have a new reference to a logically deleted record : " + id, e);
		}
	}

	public static class RecordServicesRuntimeException_CannotLogicallyDeleteRecord extends RecordServicesRuntimeException {

		public RecordServicesRuntimeException_CannotLogicallyDeleteRecord(String recordId, Exception e) {
			super("Cannot logically delete record '" + recordId + "'", e);
		}

		public RecordServicesRuntimeException_CannotLogicallyDeleteRecord(String recordId) {
			this(recordId, null);
		}
	}

	public static class RecordServicesRuntimeException_CannotPhysicallyDeleteRecord extends RecordServicesRuntimeException {

		public RecordServicesRuntimeException_CannotPhysicallyDeleteRecord(String recordId, Exception e) {
			super("Cannot physically delete record '" + recordId + "'", e);
		}

		public RecordServicesRuntimeException_CannotPhysicallyDeleteRecord(String recordId) {
			this(recordId, null);
		}
	}

	public static class RecordServicesRuntimeException_CannotRestoreRecord extends RecordServicesRuntimeException {

		public RecordServicesRuntimeException_CannotRestoreRecord(String recordId, Exception e) {
			super("Cannot restore record '" + recordId + "'", e);
		}

		public RecordServicesRuntimeException_CannotRestoreRecord(String recordId) {
			this(recordId, null);
		}
	}

	public static class RecordServicesRuntimeException_TransactionWithMoreThan1000RecordsCannotHaveTryMergeOptimisticLockingResolution
			extends RecordServicesRuntimeException {

		public RecordServicesRuntimeException_TransactionWithMoreThan1000RecordsCannotHaveTryMergeOptimisticLockingResolution() {
			super("Transaction with more than 1000 records cannot have try merge optimistic locking resolution");
		}
	}

	public static class RecordServicesRuntimeException_TransactionHasMoreThan100000Records
			extends RecordServicesRuntimeException {

		public RecordServicesRuntimeException_TransactionHasMoreThan100000Records(int length) {
			super("Transaction has " + length + " records. Limit is 100000");
		}
	}

	public static class RecordServicesRuntimeException_RecordsFlushingFailed
			extends RecordServicesRuntimeException {

		public RecordServicesRuntimeException_RecordsFlushingFailed(Exception e) {
			super("Records flushing failed", e);
		}
	}

}

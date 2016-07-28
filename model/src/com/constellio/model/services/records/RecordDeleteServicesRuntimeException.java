package com.constellio.model.services.records;

import java.util.Set;

import com.constellio.model.services.records.RecordServicesRuntimeException.RecordServicesRuntimeException_CannotPhysicallyDeleteRecord;

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

	public static class RecordDeleteServicesRuntimeException_RecordServicesErrorDuringOperation
			extends RecordDeleteServicesRuntimeException {

		public RecordDeleteServicesRuntimeException_RecordServicesErrorDuringOperation(String operation, Throwable cause) {
			super("RecordServicesException on operation '" + operation + "'", cause);
		}
	}

	public static class RecordDeleteServicesRuntimeException_CannotTotallyDeleteSchemaType
			extends RecordDeleteServicesRuntimeException {

		public RecordDeleteServicesRuntimeException_CannotTotallyDeleteSchemaType(String type) {
			super("Cannot totally delete schema type '" + type + "'");
		}
	}

	public static class RecordServicesRuntimeException_CannotPhysicallyDeleteRecord_CannotSetNullOnRecords
			extends RecordServicesRuntimeException_CannotPhysicallyDeleteRecord {

		Set<String> recordsIdsWithUnremovableReferences;
		Set<String> recordsTiltlesWithUnremovableReferences;

		public RecordServicesRuntimeException_CannotPhysicallyDeleteRecord_CannotSetNullOnRecords(String deletedRecordId,
				Set<String> recordsIdsWithUnremovableReferences, Set<String> recordsIdsTitlesWithUnremovableReferences) {
			super("Cannot physically delete record '" + deletedRecordId + "', cannot remove references to this record on ["
					+ recordsIdsWithUnremovableReferences + "]");
			this.recordsIdsWithUnremovableReferences = recordsIdsWithUnremovableReferences;
			this.recordsTiltlesWithUnremovableReferences = recordsIdsTitlesWithUnremovableReferences;
		}

		public Set<String> getRecordsIdsWithUnremovableReferences() {
			return recordsIdsWithUnremovableReferences;
		}

		public Set<String> getRecordsTiltlesWithUnremovableReferences() {
			return recordsTiltlesWithUnremovableReferences;
		}
	}

}

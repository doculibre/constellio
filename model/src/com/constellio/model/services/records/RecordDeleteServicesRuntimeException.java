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

}

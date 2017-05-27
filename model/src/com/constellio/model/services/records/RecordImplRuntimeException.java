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

		public RecordImplException_UnsupportedOperationOnUnsavedRecord(String operation, String recordId) {
			super("Operation '" + operation + "' is unsupported for unsaved record '" + recordId + "'");
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
}

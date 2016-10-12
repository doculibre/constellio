package com.constellio.model.entities.records;

import com.constellio.model.entities.schemas.Metadata;

@SuppressWarnings("serial")
public class RecordRuntimeException extends RuntimeException {

	private RecordRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	private RecordRuntimeException(String message) {
		super(message);
	}

	private RecordRuntimeException(Throwable cause) {
		super(cause);
	}

	public static class RecordIsAlreadySaved extends RecordRuntimeException {

		public RecordIsAlreadySaved(String id) {
			super("Record with id '" + id + "' is already saved");
		}

	}

	public static class CannotModifyADisconnectedRecord extends RecordRuntimeException {

		public CannotModifyADisconnectedRecord(String id) {
			super("Cannot modify disconnected record with id '" + id + "'");
		}

	}

	public static class CannotMerge extends RecordRuntimeException {

		public CannotMerge(String schema, String id, String metadataCode, Object newValue, Object value) {
			super("Record of schema '" + schema + "' with id '" + id + "' cannot merge metadata '" + metadataCode
					+ "' because it was modified to '" + newValue
					+ "' at the same time it was modified to '" + value + "'");
		}

	}

	public static class CannotChangeId extends RecordRuntimeException {

		public CannotChangeId() {
			super("Cannot change id once it has been set");
		}

	}

	public static class RecordDTORequired extends RecordRuntimeException {

		public RecordDTORequired() {
			super("RecordDTO is required for refresh");
		}

	}

	public static class RequiredMetadataArgument extends RecordRuntimeException {

		public RequiredMetadataArgument() {
			super("Metadata argument is required");
		}

	}

	public static class CannotSetManualValueInAutomaticField extends RecordRuntimeException {

		public CannotSetManualValueInAutomaticField(Metadata metadata) {
			super("Cannot set manual value in automatic metadata '" + metadata.getCode() + "'");
		}

	}

	public static class CannotSetCollectionInSingleValueMetadata extends RecordRuntimeException {

		public CannotSetCollectionInSingleValueMetadata(Metadata metadata) {
			super("Cannot set collection value in single value metadata '" + metadata.getCode() + "'");
		}

	}

	public static class CannotSetNonListValueInMultivalueMetadata extends RecordRuntimeException {

		public CannotSetNonListValueInMultivalueMetadata(Metadata metadata, Class<?> clazz) {
			super("Cannot set non-collection value of type '" + clazz.getName() + "' in multivalue value metadata '"
					+ metadata.getCode() + "'");
		}

	}

	public static class InvalidMetadata extends RecordRuntimeException {

		public InvalidMetadata(String code) {
			super("Invalid metadata '" + code + "'");
		}

	}

	public static class RecordRuntimeException_CannotModifyId extends RecordRuntimeException {

		public RecordRuntimeException_CannotModifyId() {
			super("Cannot modify id");
		}

	}

	public static class RecordRuntimeException_ValueHasBadFormat extends RecordRuntimeException {

		public RecordRuntimeException_ValueHasBadFormat(String value, String format) {
			super("Invalid value '" + value + "'. Expected format is '" + format + "'");
		}

	}
}

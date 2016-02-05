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

package com.constellio.app.ui.tools;

public class RecordContainerWebElementRuntimeException extends RuntimeException {

	public RecordContainerWebElementRuntimeException(String message) {
		super(message);
	}

	public RecordContainerWebElementRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public RecordContainerWebElementRuntimeException(Throwable cause) {
		super(cause);
	}

	public static class RecordContainerWebElementRuntimeException_NoSuchRow extends RecordContainerWebElementRuntimeException {

		public RecordContainerWebElementRuntimeException_NoSuchRow(int index) {
			super("No such row  with index '" + index + "'");
		}
	}

	public static class RecordContainerWebElementRuntimeException_NoSuchRowWithValueInColumn
			extends RecordContainerWebElementRuntimeException {

		public RecordContainerWebElementRuntimeException_NoSuchRowWithValueInColumn(String value, int columnIndex) {
			super("No such row with value '" + value + "' in column index '" + columnIndex + "'");
		}

	}
}

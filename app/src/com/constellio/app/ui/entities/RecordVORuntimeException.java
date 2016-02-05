package com.constellio.app.ui.entities;

public class RecordVORuntimeException extends RuntimeException {

	public RecordVORuntimeException(String message) {
		super(message);
	}

	public RecordVORuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public RecordVORuntimeException(Throwable cause) {
		super(cause);
	}

	public static class RecordVORuntimeException_NoSuchMetadata extends RecordVORuntimeException {
		public RecordVORuntimeException_NoSuchMetadata(String code) {
			super("No such metadata with code '" + code + "'");
		}
	}
}

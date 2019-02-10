package com.constellio.app.services.sip.ead;

public class RecordEADWriterRuntimeException extends RuntimeException {

	public RecordEADWriterRuntimeException(String message) {
		super(message);
	}

	public RecordEADWriterRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public RecordEADWriterRuntimeException(Throwable cause) {
		super(cause);
	}

	public static class RecordEADWriterRuntimeException_ErrorCreatingFile extends RecordEADWriterRuntimeException {
		public RecordEADWriterRuntimeException_ErrorCreatingFile(String eadFileZipPath, Throwable cause) {
			super("Error creating ead file '" + eadFileZipPath + "'", cause);
		}
	}
}

package com.constellio.app.services.sip.mets;

public class MetsFileWriterRuntimeException extends RuntimeException {

	public MetsFileWriterRuntimeException(String message) {
		super(message);
	}

	public MetsFileWriterRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public MetsFileWriterRuntimeException(Throwable cause) {
		super(cause);
	}

	public static class MetsFileWriterRuntimeException_ErrorCreatingFile extends MetsFileWriterRuntimeException {
		public MetsFileWriterRuntimeException_ErrorCreatingFile(String metsFileZipPath, Throwable cause) {
			super("Error creating mets file '" + metsFileZipPath + "'", cause);
		}
	}

	public static class MetsFileWriterRuntimeException_CreatedFileIsInvalid extends MetsFileWriterRuntimeException {
		public MetsFileWriterRuntimeException_CreatedFileIsInvalid(String metsFileZipPath, Throwable cause) {
			super("Error creating mets file '" + metsFileZipPath + "'", cause);
		}
	}
}

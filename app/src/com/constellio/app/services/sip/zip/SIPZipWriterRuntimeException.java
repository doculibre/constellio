package com.constellio.app.services.sip.zip;

public class SIPZipWriterRuntimeException extends RuntimeException {

	public SIPZipWriterRuntimeException(String message) {
		super(message);
	}

	public SIPZipWriterRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public SIPZipWriterRuntimeException(Throwable cause) {
		super(cause);
	}

	public static class SIPZipWriterRuntimeException_ErrorAddingToSIP extends SIPZipWriterRuntimeException {
		public SIPZipWriterRuntimeException_ErrorAddingToSIP(String sipPath, Throwable cause) {
			super("Error adding file '" + sipPath + "'", cause);
		}
	}
}

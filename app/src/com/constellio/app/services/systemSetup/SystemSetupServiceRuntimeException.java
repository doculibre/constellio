package com.constellio.app.services.systemSetup;

public class SystemSetupServiceRuntimeException extends RuntimeException {

	public SystemSetupServiceRuntimeException(String message) {
		super(message);
	}

	public SystemSetupServiceRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public SystemSetupServiceRuntimeException(Throwable cause) {
		super(cause);
	}

	public static class SystemSetupServiceRuntimeException_InvalidSetupFile extends SystemSetupServiceRuntimeException {

		public SystemSetupServiceRuntimeException_InvalidSetupFile(Throwable cause) {
			super("Invalid setup file", cause);
		}
	}

	public static class SystemSetupServiceRuntimeException_InvalidSetupFileProperty extends SystemSetupServiceRuntimeException {

		public SystemSetupServiceRuntimeException_InvalidSetupFileProperty(String property) {
			super("Invalid setup file property '" + property + "'");
		}
	}
}

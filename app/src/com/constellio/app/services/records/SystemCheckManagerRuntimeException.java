package com.constellio.app.services.records;

public class SystemCheckManagerRuntimeException extends RuntimeException {

	public SystemCheckManagerRuntimeException(String message) {
		super(message);
	}

	public SystemCheckManagerRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public SystemCheckManagerRuntimeException(Throwable cause) {
		super(cause);
	}

	public static class SystemCheckManagerRuntimeException_AlreadyRunning extends SystemCheckManagerRuntimeException {

		public SystemCheckManagerRuntimeException_AlreadyRunning() {
			super("System check is already running");
		}
	}
}

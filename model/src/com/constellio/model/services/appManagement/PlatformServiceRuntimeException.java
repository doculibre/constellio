package com.constellio.model.services.appManagement;

@SuppressWarnings("serial")
public class PlatformServiceRuntimeException extends RuntimeException {

	public PlatformServiceRuntimeException() {
	}

	public PlatformServiceRuntimeException(String message) {
		super(message);
	}

	public PlatformServiceRuntimeException(Throwable cause) {
		super(cause);
	}

	public PlatformServiceRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public static class Interrupted extends PlatformServiceRuntimeException {

		public Interrupted(Exception e) {
			super("Interrupted exception", e);
		}
	}

}

package com.constellio.model.services.encrypt;

public class EncryptionServicesRuntimeException extends RuntimeException {

	public EncryptionServicesRuntimeException(String message) {
		super(message);
	}

	public EncryptionServicesRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public EncryptionServicesRuntimeException(Throwable cause) {
		super(cause);
	}

	public static class EncryptionServicesRuntimeException_InvalidKey extends EncryptionServicesRuntimeException {
		public EncryptionServicesRuntimeException_InvalidKey(Throwable cause) {
			super("Invalid key", cause);
		}
	}
}

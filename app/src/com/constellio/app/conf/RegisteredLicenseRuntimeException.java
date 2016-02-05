package com.constellio.app.conf;

@SuppressWarnings("serial")
public class RegisteredLicenseRuntimeException extends RuntimeException {

	public RegisteredLicenseRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public RegisteredLicenseRuntimeException(String message) {
		super(message);
	}

	public RegisteredLicenseRuntimeException(Throwable cause) {
		super(cause);
	}
}
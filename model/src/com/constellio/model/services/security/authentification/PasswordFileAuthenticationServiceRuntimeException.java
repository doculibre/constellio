package com.constellio.model.services.security.authentification;

@SuppressWarnings("serial")
public class PasswordFileAuthenticationServiceRuntimeException extends RuntimeException {

	public PasswordFileAuthenticationServiceRuntimeException(String message) {
		super(message);
	}

	public PasswordFileAuthenticationServiceRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public static class InvalidPasswordException extends PasswordFileAuthenticationServiceRuntimeException {
		public InvalidPasswordException() {
			super("Empty or not setted password");
		}
	}

	public static class IncorrectPassword extends PasswordFileAuthenticationServiceRuntimeException {
		public IncorrectPassword() {
			super("Incorrect password");
		}
	}

	public static class CannotCalculateHash extends PasswordFileAuthenticationServiceRuntimeException {

		public CannotCalculateHash(String text, Exception e) {
			super("Cannot calculate hash from " + text, e);
		}

	}
}

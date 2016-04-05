package com.constellio.model.services.users;

public class UserCredentialsManagerRuntimeException extends RuntimeException {
	public UserCredentialsManagerRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public static class UserCredentialsManagerRuntimeException_CannotExecuteTransaction
			extends UserCredentialsManagerRuntimeException {
		public UserCredentialsManagerRuntimeException_CannotExecuteTransaction(Throwable cause) {
			super("Cannot execute transaction", cause);
		}
	}
}

package com.constellio.app.ui.framework.exception;

public class UserException extends Exception {
	protected UserException(String message) {
		super(message);
	}

	public static class UserDoesNotHaveAccessException extends UserException {
		public UserDoesNotHaveAccessException(String user, String page) {
			super("User " + user + " does not have access to page : " + page);
		}
	}
}

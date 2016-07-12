package com.constellio.app.api.systemManagement.services;

public class AdminHttpServletRuntimeException extends RuntimeException {

	public AdminHttpServletRuntimeException(String message) {
		super(message);
	}

	public static class AdminHttpServletRuntimeException_Unauthorized extends AdminHttpServletRuntimeException {

		public AdminHttpServletRuntimeException_Unauthorized(String message) {
			super(message);
		}
	}
}

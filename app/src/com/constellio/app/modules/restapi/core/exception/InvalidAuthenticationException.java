package com.constellio.app.modules.restapi.core.exception;

import javax.ws.rs.core.Response.Status;

public class InvalidAuthenticationException extends BaseRestApiException {

	private static final String CODE = "invalidAuthentication";

	public InvalidAuthenticationException() {
		status = Status.UNAUTHORIZED;

		buildValidationError(CODE);
	}

}

package com.constellio.app.modules.restapi.apis.v1.validation.exception;

import com.constellio.app.modules.restapi.core.exception.BaseRestApiException;

import javax.ws.rs.core.Response;

public class UnauthenticatedUserException extends BaseRestApiException {

	private static final String CODE = "unauthenticatedUser";

	public UnauthenticatedUserException() {
		status = Response.Status.FORBIDDEN;

		buildValidationError(CODE);
	}
}

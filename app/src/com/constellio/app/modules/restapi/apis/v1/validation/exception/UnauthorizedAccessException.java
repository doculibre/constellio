package com.constellio.app.modules.restapi.apis.v1.validation.exception;

import com.constellio.app.modules.restapi.core.exception.BaseRestApiException;

import javax.ws.rs.core.Response;

public class UnauthorizedAccessException extends BaseRestApiException {

	private static final String CODE = "unauthorizedAccess";

	public UnauthorizedAccessException() {
		status = Response.Status.FORBIDDEN;

		buildValidationError(CODE);
	}
}

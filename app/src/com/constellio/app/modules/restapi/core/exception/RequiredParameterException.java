package com.constellio.app.modules.restapi.core.exception;

import com.google.common.collect.ImmutableMap;

import javax.ws.rs.core.Response;

public class RequiredParameterException extends BaseRestApiException {

	private static final String CODE = "requiredParameter";

	public RequiredParameterException(String parameter) {
		status = Response.Status.BAD_REQUEST;

		buildValidationError(CODE, ImmutableMap.<String, Object>builder().put("parameter", parameter).build());
	}
}

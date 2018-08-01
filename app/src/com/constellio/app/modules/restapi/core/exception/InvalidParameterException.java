package com.constellio.app.modules.restapi.core.exception;

import com.google.common.collect.ImmutableMap;

import javax.ws.rs.core.Response;

public class InvalidParameterException extends BaseRestApiException {

	private static final String CODE = "invalidParameter";

	public InvalidParameterException(String parameter, String value) {
		status = Response.Status.BAD_REQUEST;

		buildValidationError(CODE, ImmutableMap.<String, Object>builder().put("parameter", parameter).put("value", value).build());
	}
}

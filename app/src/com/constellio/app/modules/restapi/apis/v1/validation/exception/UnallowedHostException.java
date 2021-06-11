package com.constellio.app.modules.restapi.apis.v1.validation.exception;

import com.constellio.app.modules.restapi.core.exception.BaseRestApiException;
import com.google.common.collect.ImmutableMap;

import javax.ws.rs.core.Response;

public class UnallowedHostException extends BaseRestApiException {

	private static final String CODE = "unallowedHost";

	public UnallowedHostException(String host) {
		status = Response.Status.FORBIDDEN;

		buildValidationError(CODE, ImmutableMap.<String, Object>builder().put("host", host).build());
	}
}

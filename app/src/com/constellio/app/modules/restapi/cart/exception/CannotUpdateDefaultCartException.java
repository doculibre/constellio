package com.constellio.app.modules.restapi.cart.exception;

import com.constellio.app.modules.restapi.core.exception.BaseRestApiException;
import com.google.common.collect.Maps;

import javax.ws.rs.core.Response;
import java.util.Map;

public class CannotUpdateDefaultCartException extends BaseRestApiException {

	private static final String CODE = "cannotUpdateDefaultCart";

	public CannotUpdateDefaultCartException() {
		status = Response.Status.BAD_REQUEST;

		Map<String, Object> parameters = Maps.newHashMap();
		buildValidationError(CODE, parameters);
	}

}

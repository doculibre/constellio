package com.constellio.app.modules.restapi.core.exception;

import com.google.common.collect.Maps;

import javax.ws.rs.core.Response;
import java.util.Map;

public class InvalidParameterValueCombinationException extends BaseRestApiException {

	private static final String CODE = "invalidParameterValueCombination";

	public InvalidParameterValueCombinationException(String parameter, String value, String parameter2) {
		status = Response.Status.BAD_REQUEST;

		Map<String, Object> parameters = Maps.newHashMap();
		parameters.put("parameter", parameter);
		parameters.put("value", value);
		parameters.put("parameter2", parameter2);
		buildValidationError(CODE, parameters);
	}
}

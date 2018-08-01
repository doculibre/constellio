package com.constellio.app.modules.restapi.core.exception;

import com.google.common.collect.Maps;

import javax.ws.rs.core.Response;
import java.util.Map;

public class InvalidMetadataValueException extends BaseRestApiException {

	private static final String CODE = "invalidMetadataValue";

	public InvalidMetadataValueException(String type, String value) {
		status = Response.Status.BAD_REQUEST;

		Map<String, Object> parameters = Maps.newHashMap();
		parameters.put("type", type);
		parameters.put("value", value);
		buildValidationError(CODE, parameters);
	}
}

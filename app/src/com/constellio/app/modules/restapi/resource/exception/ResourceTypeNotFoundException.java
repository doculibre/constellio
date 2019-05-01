package com.constellio.app.modules.restapi.resource.exception;

import com.constellio.app.modules.restapi.core.exception.BaseRestApiException;
import com.google.common.collect.Maps;

import javax.ws.rs.core.Response;
import java.util.Map;

public class ResourceTypeNotFoundException extends BaseRestApiException {

	// FIXME change to generic code?
	private static final String CODE = "documentTypeNotFound";

	public ResourceTypeNotFoundException(String field, String value) {
		status = Response.Status.NOT_FOUND;

		Map<String, Object> parameters = Maps.newHashMap();
		parameters.put("field", field);
		parameters.put("value", value);
		buildValidationError(CODE, parameters);
	}

}

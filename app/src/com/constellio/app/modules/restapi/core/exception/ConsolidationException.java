package com.constellio.app.modules.restapi.core.exception;

import com.google.common.collect.Maps;

import javax.ws.rs.core.Response.Status;
import java.util.Map;

public class ConsolidationException extends BaseRestApiException {

	private static final String CODE = "consolidationException";

	public ConsolidationException(String msg) {
		status = Status.BAD_REQUEST;

		Map<String, Object> parameters = Maps.newHashMap();
		parameters.put("msg", msg);
		buildValidationError(CODE, parameters);
	}

}

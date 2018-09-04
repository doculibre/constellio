package com.constellio.app.modules.restapi.core.exception;

import com.google.common.collect.Maps;

import javax.ws.rs.core.Response;
import java.util.Map;

public class InvalidDateCombinationException extends BaseRestApiException {

	private static final String CODE = "invalidDateCombination";

	public InvalidDateCombinationException(String startDate, String endDate) {
		status = Response.Status.BAD_REQUEST;

		Map<String, Object> parameters = Maps.newHashMap();
		parameters.put("start", startDate);
		parameters.put("end", endDate);
		buildValidationError(CODE, parameters);
	}
}

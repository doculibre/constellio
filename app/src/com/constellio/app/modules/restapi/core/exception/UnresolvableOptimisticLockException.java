package com.constellio.app.modules.restapi.core.exception;

import com.google.common.collect.Maps;

import javax.ws.rs.core.Response;
import java.util.Map;

public class UnresolvableOptimisticLockException extends BaseRestApiException {

	private static final String CODE = "unresolvableOptimisticLock";

	public UnresolvableOptimisticLockException(String id) {
		status = Response.Status.CONFLICT;

		Map<String, Object> parameters = Maps.newHashMap();
		parameters.put("id", id);
		buildValidationError(CODE, parameters);
	}

}

package com.constellio.app.modules.restapi.core.exception;

import com.google.common.collect.Maps;

import javax.ws.rs.core.Response;
import java.util.Map;

public class OptimisticLockRuntimeException extends BaseRestApiException {

	private static final String CODE = "optimisticLock";

	public OptimisticLockRuntimeException(String id, String version, long currentVersion) {
		status = Response.Status.PRECONDITION_FAILED;

		Map<String, Object> parameters = Maps.newHashMap();
		parameters.put("id", id);
		parameters.put("etag", version);
		parameters.put("currentEtag", currentVersion);
		buildValidationError(CODE, parameters);
	}

}

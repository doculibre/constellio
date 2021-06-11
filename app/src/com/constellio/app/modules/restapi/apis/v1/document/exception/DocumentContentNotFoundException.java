package com.constellio.app.modules.restapi.apis.v1.document.exception;

import com.constellio.app.modules.restapi.core.exception.BaseRestApiException;
import com.google.common.collect.Maps;

import javax.ws.rs.core.Response;
import java.util.Map;

public class DocumentContentNotFoundException extends BaseRestApiException {

	private static final String CODE = "documentContentNotFound";

	public DocumentContentNotFoundException(String id, String version) {
		status = Response.Status.NOT_FOUND;

		Map<String, Object> parameters = Maps.newHashMap();
		parameters.put("id", id);
		parameters.put("version", version);
		buildValidationError(CODE, parameters);
	}

}

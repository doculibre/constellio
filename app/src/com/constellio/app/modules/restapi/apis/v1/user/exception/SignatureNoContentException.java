package com.constellio.app.modules.restapi.apis.v1.user.exception;

import com.constellio.app.modules.restapi.core.exception.BaseRestApiException;
import com.google.common.collect.Maps;

import javax.ws.rs.core.Response;
import java.util.Map;

public class SignatureNoContentException extends BaseRestApiException {

	public SignatureNoContentException() {
		status = Response.Status.NO_CONTENT;

		Map<String, Object> parameters = Maps.newHashMap();
		buildValidationError("", parameters);
	}

}

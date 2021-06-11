package com.constellio.app.modules.restapi.apis.v1.user.exception;

import com.constellio.app.modules.restapi.core.exception.BaseRestApiException;
import com.google.common.collect.Maps;

import javax.ws.rs.core.Response;
import java.util.Map;

public class SignatureContentNotFoundException extends BaseRestApiException {

	private static final String CODE = "signatureContentNotFound";

	public SignatureContentNotFoundException() {
		status = Response.Status.NOT_FOUND;

		Map<String, Object> parameters = Maps.newHashMap();
		buildValidationError(CODE, parameters);
	}

}

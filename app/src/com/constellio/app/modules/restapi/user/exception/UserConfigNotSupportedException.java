package com.constellio.app.modules.restapi.user.exception;

import com.constellio.app.modules.restapi.core.exception.BaseRestApiException;
import com.google.common.collect.Maps;

import javax.ws.rs.core.Response;
import java.util.Map;

public class UserConfigNotSupportedException extends BaseRestApiException {

	private static final String CODE = "userConfigNotSupported";

	public UserConfigNotSupportedException() {
		status = Response.Status.BAD_REQUEST;

		Map<String, Object> parameters = Maps.newHashMap();
		buildValidationError(CODE, parameters);
	}

}

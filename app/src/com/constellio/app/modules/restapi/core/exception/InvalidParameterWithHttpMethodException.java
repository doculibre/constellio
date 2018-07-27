package com.constellio.app.modules.restapi.core.exception;

import com.google.common.collect.Maps;

import javax.ws.rs.core.Response;
import java.util.Map;

public class InvalidParameterWithHttpMethodException extends BaseRestApiException {

    private static final String CODE = "invalidParameterWithHttpMethod";

    public InvalidParameterWithHttpMethodException(String parameter, String method) {
        status = Response.Status.BAD_REQUEST;

        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("parameter", parameter);
        parameters.put("method", method);
        buildValidationError(CODE, parameters);
    }
}

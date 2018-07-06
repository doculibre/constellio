package com.constellio.app.modules.restapi.core.exception;

import com.google.common.collect.Maps;

import javax.ws.rs.core.Response;
import java.util.Map;

public class AtLeastOneParameterRequiredException extends BaseRestApiException {

    private static final String CODE = "atLeastOneParameterRequired";

    public AtLeastOneParameterRequiredException(String parameter1, String parameter2) {
        status = Response.Status.BAD_REQUEST;

        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("parameter1", parameter1);
        parameters.put("parameter2", parameter2);
        buildValidationError(CODE, parameters);
    }
}

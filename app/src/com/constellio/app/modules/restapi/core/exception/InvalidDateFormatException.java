package com.constellio.app.modules.restapi.core.exception;

import com.google.common.collect.Maps;

import javax.ws.rs.core.Response;
import java.util.Map;

public class InvalidDateFormatException extends BaseRestApiException {

    private static final String CODE = "invalidDateFormat";

    public InvalidDateFormatException(String date, String pattern) {
        status = Response.Status.BAD_REQUEST;

        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("date", date);
        parameters.put("pattern", pattern);
        buildValidationError(CODE, parameters);
    }
}

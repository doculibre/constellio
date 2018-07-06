package com.constellio.app.modules.restapi.core.exception;

import com.google.common.collect.ImmutableMap;

import javax.ws.rs.core.Response;

public class RecordNotFoundException extends BaseRestApiException {

    private static final String CODE = "recordNotFound";

    public RecordNotFoundException(String id) {
        status = Response.Status.NOT_FOUND;

        buildValidationError(CODE, ImmutableMap.<String, Object>builder().put("id", id).build());
    }
}

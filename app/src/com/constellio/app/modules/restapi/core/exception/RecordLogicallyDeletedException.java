package com.constellio.app.modules.restapi.core.exception;

import com.google.common.collect.ImmutableMap;

import javax.ws.rs.core.Response;

public class RecordLogicallyDeletedException extends BaseRestApiException {

    private static final String CODE = "recordLogicallyDeleted";

    public RecordLogicallyDeletedException(String id) {
        status = Response.Status.BAD_REQUEST;

        buildValidationError(CODE, ImmutableMap.<String, Object>builder().put("id", id).build());
    }
}

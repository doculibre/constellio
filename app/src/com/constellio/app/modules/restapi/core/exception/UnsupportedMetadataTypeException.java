package com.constellio.app.modules.restapi.core.exception;

import com.google.common.collect.ImmutableMap;

import javax.ws.rs.core.Response;

public class UnsupportedMetadataTypeException extends BaseRestApiException {

    private static final String CODE = "unsupportedMetadataType";

    public UnsupportedMetadataTypeException(String type) {
        status = Response.Status.BAD_REQUEST;

        buildValidationError(CODE, ImmutableMap.<String, Object>builder().put("type", type).build());
    }
}

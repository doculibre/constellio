package com.constellio.app.modules.restapi.core.exception;

import com.google.common.collect.ImmutableMap;

import javax.ws.rs.core.Response;

public class MetadataNotMultivalueException extends BaseRestApiException {

    private static final String CODE = "metadataNotMultivalue";

    public MetadataNotMultivalueException(String code) {
        status = Response.Status.BAD_REQUEST;

        buildValidationError(CODE, ImmutableMap.<String, Object>builder().put("code", code).build());
    }
}

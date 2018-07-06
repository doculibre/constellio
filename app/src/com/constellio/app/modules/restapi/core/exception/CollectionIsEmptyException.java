package com.constellio.app.modules.restapi.core.exception;

import com.google.common.collect.Maps;

import javax.ws.rs.core.Response;
import java.util.Map;

public class CollectionIsEmptyException extends BaseRestApiException {

    private static final String CODE = "collectionIsEmpty";

    public CollectionIsEmptyException(String collection) {
        status = Response.Status.BAD_REQUEST;

        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("collection", collection);
        buildValidationError(CODE, parameters);
    }
}

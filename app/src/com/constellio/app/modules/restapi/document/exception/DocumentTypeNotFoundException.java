package com.constellio.app.modules.restapi.document.exception;

import com.constellio.app.modules.restapi.core.exception.BaseRestApiException;
import com.google.common.collect.Maps;

import javax.ws.rs.core.Response;
import java.util.Map;

public class DocumentTypeNotFoundException extends BaseRestApiException {

    private static final String CODE = "documentTypeNotFound";

    public DocumentTypeNotFoundException(String field, String value) {
        status = Response.Status.NOT_FOUND;

        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("field", field);
        parameters.put("value", value);
        buildValidationError(CODE, parameters);
    }

}

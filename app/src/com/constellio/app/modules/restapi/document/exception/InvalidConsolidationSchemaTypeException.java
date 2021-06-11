package com.constellio.app.modules.restapi.document.exception;

import com.constellio.app.modules.restapi.core.exception.BaseRestApiException;
import com.google.common.collect.Maps;

import javax.ws.rs.core.Response.Status;
import java.util.Map;

public class InvalidConsolidationSchemaTypeException extends BaseRestApiException {
	public static final String CODE = "PdfGeneratorAsyncTask.invalidSchemaType";

	public InvalidConsolidationSchemaTypeException(String recordId, String schemaCode) {
		status = Status.BAD_REQUEST;

		Map<String, Object> parameters = Maps.newHashMap();
		parameters.put("id", recordId);
		parameters.put("schemaCode", schemaCode);

		buildValidationError(CODE, parameters);
	}
}

package com.constellio.app.modules.restapi.core.exception;

import com.google.common.collect.Maps;

import javax.ws.rs.core.Response.Status;
import java.util.Map;

public class CannotReadContentException extends BaseRestApiException {

	public static final String CODE = "PdfGeneratorAsyncTask.cannotReadContent";

	public CannotReadContentException(String documentId) {
		status = Status.BAD_REQUEST;

		Map<String, Object> parameters = Maps.newHashMap();
		parameters.put("id", documentId);

		buildValidationError(CODE, parameters);
	}
}

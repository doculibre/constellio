package com.constellio.app.modules.restapi.core.exception;

import javax.ws.rs.core.Response.Status;

public class ConsolidationException extends BaseRestApiException {

	private static final String CODE = "PdfGeneratorAsyncTask.globalError";

	public ConsolidationException() {
		status = Status.BAD_REQUEST;

		buildValidationError(CODE);
	}
}

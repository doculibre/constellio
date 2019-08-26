package com.constellio.app.modules.restapi.core.exception;

import com.google.common.collect.ImmutableMap;

import javax.ws.rs.core.Response.Status;

public class RecordCopyNotPermittedException extends BaseRestApiException {

	private static final String CODE = "recordCopyNotPermitted";

	public RecordCopyNotPermittedException(String id) {
		status = Status.BAD_REQUEST;

		buildValidationError(CODE, ImmutableMap.<String, Object>builder().put("id", id).build());
	}
}

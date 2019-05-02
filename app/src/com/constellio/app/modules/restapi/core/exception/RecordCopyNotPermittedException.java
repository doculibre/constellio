package com.constellio.app.modules.restapi.core.exception;

import com.constellio.app.modules.restapi.core.util.HttpResponseStatus;
import com.google.common.collect.ImmutableMap;

public class RecordCopyNotPermittedException extends BaseRestApiException {

	private static final String CODE = "recordCopyNotPermitted";

	public RecordCopyNotPermittedException(String id) {
		status = HttpResponseStatus.UNPROCESSABLE_ENTITY;

		buildValidationError(CODE, ImmutableMap.<String, Object>builder().put("id", id).build());
	}
}

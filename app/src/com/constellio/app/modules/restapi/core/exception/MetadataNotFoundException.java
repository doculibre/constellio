package com.constellio.app.modules.restapi.core.exception;

import com.google.common.collect.ImmutableMap;

import javax.ws.rs.core.Response;

public class MetadataNotFoundException extends BaseRestApiException {

	private static final String CODE = "metadataNotFound";

	public MetadataNotFoundException(String code) {
		status = Response.Status.NOT_FOUND;

		buildValidationError(CODE, ImmutableMap.<String, Object>builder().put("code", code).build());
	}
}

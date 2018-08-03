package com.constellio.app.modules.restapi.core.exception;

import com.google.common.collect.ImmutableMap;

import javax.ws.rs.core.Response;

public class MetadataReferenceNotAllowedException extends BaseRestApiException {

	private static final String CODE = "metadataReferenceNotAllowed";

	public MetadataReferenceNotAllowedException(String type, String code) {
		status = Response.Status.BAD_REQUEST;

		buildValidationError(CODE, ImmutableMap.<String, Object>builder().put("type", type).put("code", code).build());
	}
}

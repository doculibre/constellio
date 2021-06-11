package com.constellio.app.modules.restapi.core.exception;

import com.google.common.collect.ImmutableMap;

import javax.ws.rs.core.Response;

public class CollectionNotFoundException extends BaseRestApiException {

	private static final String CODE = "collectionNotFound";

	public CollectionNotFoundException(String collection) {
		status = Response.Status.NOT_FOUND;

		buildValidationError(CODE, ImmutableMap.<String, Object>builder().put("code", collection).build());
	}
}

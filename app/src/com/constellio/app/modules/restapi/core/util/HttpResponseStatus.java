package com.constellio.app.modules.restapi.core.util;

import javax.ws.rs.core.Response.Status.Family;
import javax.ws.rs.core.Response.StatusType;

public enum HttpResponseStatus implements StatusType {

	UNPROCESSABLE_ENTITY(422, "Unprocessable Entity");

	private final int code;
	private final String reason;
	private final Family family;

	HttpResponseStatus(final int statusCode, final String reasonPhrase) {
		this.code = statusCode;
		this.reason = reasonPhrase;
		this.family = Family.familyOf(statusCode);
	}

	@Override
	public int getStatusCode() {
		return code;
	}

	@Override
	public Family getFamily() {
		return family;
	}

	@Override
	public String getReasonPhrase() {
		return reason;
	}
}

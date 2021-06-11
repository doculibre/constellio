package com.constellio.app.servlet;

import com.constellio.app.modules.restapi.core.exception.BaseRestApiException;

import javax.ws.rs.core.Response.Status;

public class BaseServletServiceException extends BaseRestApiException {
	public static class BaseServletServiceException_CannotReadEntity extends BaseServletServiceException {
		public BaseServletServiceException_CannotReadEntity() {
			status = Status.BAD_REQUEST;
			buildValidationError("cannotReadEntity");
		}
	}

	public static class BaseServletServiceException_CannotWriteEntity extends BaseServletServiceException {
		public BaseServletServiceException_CannotWriteEntity() {
			status = Status.BAD_REQUEST;
			buildValidationError("cannotWriteEntity");
		}
	}
}

package com.constellio.app.modules.rm.servlet;

import com.constellio.app.servlet.BaseServletServiceException;

import javax.ws.rs.core.Response.Status;

public class SignatureExternalAccessServiceException extends BaseServletServiceException {
	public static class SignatureExternalAccessServiceException_CannotCreateAccess extends SignatureExternalAccessServiceException {
		public SignatureExternalAccessServiceException_CannotCreateAccess() {
			status = Status.BAD_REQUEST;
			buildValidationError("cannotCreateAccess");
		}
	}

	public static class SignatureExternalAccessServiceException_CannotSendEmail extends SignatureExternalAccessServiceException {
		public SignatureExternalAccessServiceException_CannotSendEmail() {
			status = Status.BAD_REQUEST;
			buildValidationError("cannotSendEmail");
		}
	}

	public static class SignatureExternalAccessServiceException_EmailServerNotConfigured extends SignatureExternalAccessServiceException {
		public SignatureExternalAccessServiceException_EmailServerNotConfigured() {
			status = Status.CONFLICT;
			buildValidationError("emailServerNotConfigured");
		}
	}

	public static class SignatureExternalAccessServiceException_NoSignCertificate extends SignatureExternalAccessServiceException {
		public SignatureExternalAccessServiceException_NoSignCertificate() {
			status = Status.CONFLICT;
			buildValidationError("noSignCertificate");
		}
	}
}

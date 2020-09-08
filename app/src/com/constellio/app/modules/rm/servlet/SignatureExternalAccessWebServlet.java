package com.constellio.app.modules.rm.servlet;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class SignatureExternalAccessWebServlet extends HttpServlet {
	public static final String HEADER_PARAM_AUTH = "Authorization";

	public static final String PARAM_ID = "id";
	public static final String PARAM_TOKEN = "token";
	public static final String PARAM_SERVICE_KEY = "serviceKey";
	public static final String PARAM_DOCUMENT = "document";
	public static final String PARAM_EXTERNAL_USER_FULLNAME = "externalUserFullname";
	public static final String PARAM_EXTERNAL_USER_EMAIL = "externalUserEmail";
	public static final String PARAM_EXPIRATION_DATE = "expirationDate";
	public static final String PARAM_LANGUAGE = "language";

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		SignatureExternalAccessService service = new SignatureExternalAccessService();

		try {
			String location = service.accessExternalSignature(req.getParameter(PARAM_ID), req.getParameter(PARAM_TOKEN),
					req.getParameter(PARAM_LANGUAGE), req.getRemoteAddr());

			resp.setStatus(HttpServletResponse.SC_OK);
			resp.sendRedirect(location);
		} catch (SignatureExternalAccessServiceException e) {
			resp.sendError(e.getStatus(), e.getMessage());
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		SignatureExternalAccessService service = new SignatureExternalAccessService();

		try {
			service.createExternalSignatureUrl(req.getHeader(HEADER_PARAM_AUTH),
					req.getParameter(PARAM_SERVICE_KEY), req.getParameter(PARAM_DOCUMENT),
					req.getParameter(PARAM_EXTERNAL_USER_FULLNAME), req.getParameter(PARAM_EXTERNAL_USER_EMAIL),
					req.getParameter(PARAM_EXPIRATION_DATE), req.getParameter(PARAM_LANGUAGE));

			resp.setStatus(HttpServletResponse.SC_OK);
		} catch (SignatureExternalAccessServiceException e) {
			resp.sendError(e.getStatus(), e.getMessage());
		}
	}
}

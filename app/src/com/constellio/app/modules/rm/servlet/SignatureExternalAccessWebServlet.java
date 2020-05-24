package com.constellio.app.modules.rm.servlet;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class SignatureExternalAccessWebServlet extends HttpServlet {
	private static final String HEADER_PARAM_AUTH = "Authorization";

	private static final String PARAM_SERVICE_KEY = "serviceKey";
	private static final String PARAM_DOCUMENT = "document";
	private static final String PARAM_EXPIRATION_DATE = "expirationDate";

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		SignatureExternalAccessService service = new SignatureExternalAccessService();

		try {
			String url = service.createExternalSignatureUrl(req.getHeader(HEADER_PARAM_AUTH),
					req.getParameter(PARAM_SERVICE_KEY), req.getParameter(PARAM_DOCUMENT),
					req.getParameter(PARAM_EXPIRATION_DATE));

			resp.setStatus(HttpServletResponse.SC_OK);
			resp.getOutputStream().print(url);
		} catch (SignatureExternalAccessServiceException e) {
			resp.sendError(e.getStatus(), e.getMessage());
		}
	}
}

package com.constellio.app.modules.rm.servlet;

import com.constellio.app.modules.restapi.core.exception.AtLeastOneParameterRequiredException;
import com.constellio.app.modules.restapi.core.exception.BaseRestApiException;
import com.constellio.app.modules.restapi.core.exception.InvalidParameterCombinationException;
import com.constellio.app.modules.restapi.core.util.AuthorizationUtils;
import com.constellio.app.servlet.BaseHttpServlet;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.constellio.app.ui.i18n.i18n.$;

public class SignatureExternalAccessWebServlet extends BaseHttpServlet {
	public static final String HEADER_PARAM_AUTH = "Authorization";

	public static final String PARAM_ID = "id";
	public static final String PARAM_TOKEN = "token";
	public static final String PARAM_SERVICE_KEY = "serviceKey";
	public static final String PARAM_DOCUMENT = "document";
	public static final String PARAM_INTERNAL_USER = "internalUser";
	public static final String PARAM_EXTERNAL_USER_FULLNAME = "externalUserFullname";
	public static final String PARAM_EXTERNAL_USER_EMAIL = "externalUserEmail";
	public static final String PARAM_EXPIRATION_DATE = "expirationDate";
	public static final String PARAM_LANGUAGE = "language";

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		try {
			String location = accessExternalSignature(req.getParameter(PARAM_ID), req.getParameter(PARAM_TOKEN),
					req.getParameter(PARAM_LANGUAGE), req.getRemoteAddr());

			resp.setStatus(HttpServletResponse.SC_OK);
			resp.sendRedirect(location);
		} catch (BaseRestApiException e) {
			resp.setStatus(e.getStatus().getStatusCode());
			resp.setContentType("application/json");
			resp.getWriter().write($(e.getValidationError()));
		}
	}

	private String accessExternalSignature(String accessId, String token, String language, String ipAddress) {
		validateRequiredParameter(accessId, PARAM_ID, true);
		validateRequiredParameter(token, PARAM_TOKEN, true);
		validateRequiredParameter(language, PARAM_LANGUAGE, true);

		SignatureExternalAccessService service = new SignatureExternalAccessService();
		return service.accessExternalSignature(token, accessId, language, ipAddress);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		try {
			sendSignatureRequest(req.getHeader(HEADER_PARAM_AUTH), req.getParameter(PARAM_SERVICE_KEY),
					req.getParameter(PARAM_DOCUMENT), req.getParameter(PARAM_INTERNAL_USER),
					req.getParameter(PARAM_EXTERNAL_USER_FULLNAME), req.getParameter(PARAM_EXTERNAL_USER_EMAIL),
					req.getParameter(PARAM_EXPIRATION_DATE), req.getParameter(PARAM_LANGUAGE));

			resp.setStatus(HttpServletResponse.SC_OK);
		} catch (BaseRestApiException e) {
			resp.setStatus(e.getStatus().getStatusCode());
			resp.setContentType("application/json");
			resp.getWriter().write($(e.getValidationError().getValidatorErrorCode()));
		}
	}

	private void sendSignatureRequest(String authentication, String serviceKey, String documentId,
									  String internalUserId, String externalUserFullname, String externalUserEmail,
									  String expirationDate, String language) {
		validateAuthentication(authentication);
		String authToken = AuthorizationUtils.getToken(authentication);

		validateRequiredParameter(serviceKey, PARAM_SERVICE_KEY, true);
		validateRequiredParameter(documentId, PARAM_DOCUMENT, true);

		if (StringUtils.isBlank(internalUserId) && StringUtils.isBlank(externalUserFullname)) {
			throw new AtLeastOneParameterRequiredException(PARAM_INTERNAL_USER, PARAM_EXTERNAL_USER_FULLNAME);
		}

		if (StringUtils.isNotBlank(internalUserId) && StringUtils.isNotBlank(externalUserFullname)) {
			throw new InvalidParameterCombinationException(PARAM_INTERNAL_USER, PARAM_EXTERNAL_USER_FULLNAME);
		}

		validateRequiredParameter(expirationDate, PARAM_EXPIRATION_DATE, true);
		validateRequiredParameter(language, PARAM_LANGUAGE, true);

		SignatureExternalAccessService service = new SignatureExternalAccessService();

		boolean isExternalUser = StringUtils.isBlank(internalUserId);
		if (isExternalUser) {
			validateRequiredParameter(externalUserEmail, PARAM_EXTERNAL_USER_EMAIL, true);
		}

		service.sendSignatureRequest(authToken, serviceKey, documentId, internalUserId, externalUserFullname,
				externalUserEmail, expirationDate, language);
	}
}

package com.constellio.app.servlet.userSecurity;

import com.constellio.app.modules.restapi.core.exception.BaseRestApiException;
import com.constellio.app.modules.restapi.core.util.AuthorizationUtils;
import com.constellio.app.servlet.BaseHttpServlet;
import com.constellio.app.servlet.userSecurity.dto.UserSecurityInfoDto;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.constellio.app.ui.i18n.i18n.$;

public class UserSecurityInfoWebServlet extends BaseHttpServlet {
	public static final String HEADER_PARAM_AUTH = "Authorization";

	public static final String PARAM_SERVICE_KEY = "serviceKey";

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		try {
			UserSecurityInfoDto info = getSecurityInfo(req.getHeader(HEADER_PARAM_AUTH),
					req.getParameter(PARAM_SERVICE_KEY));

			resp.setStatus(HttpServletResponse.SC_OK);
			writeJSONEntity(resp, info);
		} catch (BaseRestApiException e) {
			resp.setStatus(e.getStatus().getStatusCode());
			resp.setContentType("application/json");
			resp.getWriter().write($(e.getValidationError()));
		}
	}

	private UserSecurityInfoDto getSecurityInfo(String authentication, String serviceKey) {
		validateAuthentication(authentication);
		String authToken = AuthorizationUtils.getToken(authentication);

		validateRequiredParameter(serviceKey, PARAM_SERVICE_KEY, true);

		UserSecurityInfoService service = new UserSecurityInfoService();
		return service.getSecurityInfo(authToken, serviceKey);
	}
}

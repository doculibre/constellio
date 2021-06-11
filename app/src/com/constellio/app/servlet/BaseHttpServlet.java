package com.constellio.app.servlet;

import com.constellio.app.modules.restapi.core.exception.BaseRestApiException;
import com.constellio.app.modules.restapi.core.exception.InvalidAuthenticationException;
import com.constellio.app.modules.restapi.core.exception.RequiredParameterException;
import com.constellio.app.modules.restapi.core.util.AuthorizationUtils;
import com.constellio.app.servlet.BaseServletServiceException.BaseServletServiceException_CannotReadEntity;
import com.constellio.app.servlet.BaseServletServiceException.BaseServletServiceException_CannotWriteEntity;
import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;

import static com.constellio.app.ui.i18n.i18n.$;

public abstract class BaseHttpServlet extends HttpServlet {

	protected void sendError(HttpServletResponse resp, BaseRestApiException e)
			throws IOException {
		BaseServletErrorResponse error = BaseServletErrorResponse.builder()
				.code(e.getStatus().getStatusCode())
				.description(e.getStatus().getReasonPhrase())
				.message($(e.getValidationError()))
				.build();

		resp.setStatus(e.getStatus().getStatusCode());
		resp.setContentType("application/json");

		Gson gson = new GsonBuilder().create();
		resp.getWriter().write(gson.toJson(error));
	}

	protected <T> T readJSONEntity(HttpServletRequest req, Class<T> entityClass) {
		StringBuilder jsonBuilder = new StringBuilder();
		String line = null;
		try {
			BufferedReader reader = req.getReader();
			while ((line = reader.readLine()) != null) {
				jsonBuilder.append(line);
			}

			Gson gson = new GsonBuilder().create();
			return gson.fromJson(jsonBuilder.toString(), entityClass);
		} catch (Exception e) {
			throw new BaseServletServiceException_CannotReadEntity();
		}
	}

	protected <T> void writeJSONEntity(HttpServletResponse resp, T entity) {
		try {
			Gson gson = new Gson();
			String json = gson.toJson(entity);

			resp.setContentType("application/json");
			resp.getWriter().write(json);
		} catch (Exception e) {
			throw new BaseServletServiceException_CannotWriteEntity();
		}
	}

	protected void validateAuthentication(String authentication) {
		if (Strings.isNullOrEmpty(authentication)) {
			throw new InvalidAuthenticationException();
		}

		String scheme = AuthorizationUtils.getScheme(authentication);
		if (scheme == null || !scheme.equals(AuthorizationUtils.SCHEME)) {
			throw new InvalidAuthenticationException();
		}
	}

	protected void validateRequiredParameter(Object parameter, String parameterName, boolean validateEmpty) {
		if (parameter == null) {
			throw new RequiredParameterException(parameterName);
		}

		if (validateEmpty && parameter instanceof String && StringUtils.isBlank((String) parameter)) {
			throw new RequiredParameterException(parameterName);
		}
	}
}

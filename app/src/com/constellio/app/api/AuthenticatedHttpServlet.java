package com.constellio.app.api;

import com.constellio.app.api.search.isNotAuthenticatedException;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.users.SystemWideUserInfos;
import org.jetbrains.annotations.NotNull;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public abstract class AuthenticatedHttpServlet extends HttpServlet {

	private static final String INVALID_AUTHENTICATION = "Invalid authentication information";
	private static final String INVALID_HTTP_METHOD = "Invalid http method. This servlet only supports ";

	@Override
	protected final void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			doGet(authenticate(req), req, resp);
		} catch (isNotAuthenticatedException e) {
			resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, INVALID_AUTHENTICATION);
		} catch (UnsupportedOperationException e) {
			resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, INVALID_AUTHENTICATION + getSupportedHttpMethodsAsString());
		}
	}

	@Override
	protected final void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			doPost(authenticate(req), req, resp);
		} catch (isNotAuthenticatedException e) {
			resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, INVALID_AUTHENTICATION + getSupportedHttpMethodsAsString());
		} catch (UnsupportedOperationException e) {
			resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, INVALID_AUTHENTICATION + getSupportedHttpMethodsAsString());
		}
	}

	@Override
	protected final void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			doPut(authenticate(req), req, resp);
		} catch (isNotAuthenticatedException e) {
			resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, INVALID_AUTHENTICATION + getSupportedHttpMethodsAsString());
		} catch (UnsupportedOperationException e) {
			resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, INVALID_AUTHENTICATION + getSupportedHttpMethodsAsString());
		}
	}

	@Override
	protected final void doDelete(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		try {
			doDelete(authenticate(req), req, resp);
		} catch (isNotAuthenticatedException e) {
			resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, INVALID_AUTHENTICATION + getSupportedHttpMethodsAsString());
		} catch (UnsupportedOperationException e) {
			resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, INVALID_AUTHENTICATION + getSupportedHttpMethodsAsString());
		}
	}

	@Override
	protected final void doOptions(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		try {
			doOptions(authenticate(req), req, resp);
		} catch (isNotAuthenticatedException e) {
			resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, INVALID_AUTHENTICATION + getSupportedHttpMethodsAsString());
		} catch (UnsupportedOperationException e) {
			resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, INVALID_AUTHENTICATION + getSupportedHttpMethodsAsString());
		}
	}

	protected void doGet(SystemWideUserInfos user, HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		throw new UnsupportedOperationException();
	}

	protected void doPost(SystemWideUserInfos user, HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		throw new UnsupportedOperationException();
	}

	protected void doPut(SystemWideUserInfos user, HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		throw new UnsupportedOperationException();
	}

	protected void doOptions(SystemWideUserInfos user, HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		throw new UnsupportedOperationException();
	}

	protected void doDelete(SystemWideUserInfos user, HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		throw new UnsupportedOperationException();
	}

	@NotNull
	protected SystemWideUserInfos authenticate(HttpServletRequest request) {
		HttpServletRequestAuthenticator authenticator = new HttpServletRequestAuthenticator(modelLayerFactory());
		SystemWideUserInfos user = authenticator.authenticate(request);
		if (user == null) {
			throw new isNotAuthenticatedException();
		}

		return user;
	}

	protected ModelLayerFactory modelLayerFactory() {
		return getConstellioFactories().getModelLayerFactory();
	}

	protected AppLayerFactory appLayerFactory() {
		return getConstellioFactories().getAppLayerFactory();
	}

	protected synchronized ConstellioFactories getConstellioFactories() {
		return ConstellioFactories.getInstance();
	}

	private String getSupportedHttpMethodsAsString() {
		return getSupportedHttpMethods().stream().collect(Collectors.joining(", "));
	}

	protected abstract List<String> getSupportedHttpMethods();
}

package com.constellio.app.servlet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.apache.chemistry.opencmis.server.impl.webservices.CmisWebServicesServlet;

import com.constellio.model.conf.FoldersLocator;
import com.constellio.model.conf.FoldersLocatorMode;

public class ConstellioCmisWebServicesServlet extends CmisWebServicesServlet {

	@Override
	public void init(ServletConfig config)
			throws ServletException {
		if (isEnabled()) {
			super.init(config);
		}
	}

	@Override
	public void handleRequest(final HttpServletRequest request, HttpServletResponse response)
			throws ServletException {
		HttpServletRequestWrapper requestProxyWrapper = new HttpServletRequestWrapper(request) {
			public String getScheme() {
				String scheme = request.getHeader("X-Forwarded-Proto");
				if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
					scheme = request.getScheme();
				}
				return scheme;
			}

			@Override
			public int getServerPort() {
				int port = request.getServerPort();
				String scheme = request.getHeader("X-Forwarded-Proto");
				if ("https".equalsIgnoreCase(scheme)) {
					port = 443;
				}
				return port;
			}
		};
		super.handleRequest(requestProxyWrapper, response);
	}

	private boolean isEnabled() {
		FoldersLocator foldersLocator = new FoldersLocator();
		if (foldersLocator.getFoldersLocatorMode() == FoldersLocatorMode.WRAPPER) {
			return true;
		} else {
			return "true".equals(System.getProperty("cmisEnabled"));
		}
	}
}

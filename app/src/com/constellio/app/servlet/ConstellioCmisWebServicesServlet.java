package com.constellio.app.servlet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.apache.chemistry.opencmis.server.impl.webservices.CmisWebServicesServlet;

;import java.io.IOException;

public class ConstellioCmisWebServicesServlet extends CmisWebServicesServlet {

	@Override
	public void init(ServletConfig config)
			throws ServletException {
		super.init(config);
	}

	@Override
	public void handleRequest(final HttpServletRequest request, HttpServletResponse response) throws ServletException {
		HttpServletRequestWrapper requestProxyWrapper = new HttpServletRequestWrapper(request) {
			public String getScheme() {
				String scheme = request.getHeader("X-Forwarded-Proto");
				if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
					scheme = request.getScheme();
				}
				return scheme;
			}
		};
		super.handleRequest(requestProxyWrapper, response);
	}
}

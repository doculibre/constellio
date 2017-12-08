package com.constellio.app.servlet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.apache.chemistry.opencmis.server.impl.atompub.CmisAtomPubServlet;

import java.io.IOException;

public class ConstellioCmisAtomPubServlet extends CmisAtomPubServlet {

	@Override
	public void init(ServletConfig config)
			throws ServletException {
		if (ConstellioCmisServices.enabled) {
			super.init(config);
		}
	}

	@Override
	protected void service(final HttpServletRequest request, HttpServletResponse response) throws ServletException,
			IOException {
		HttpServletRequestWrapper requestProxyWrapper = new HttpServletRequestWrapper(request) {
			public String getScheme() {
				String scheme = request.getHeader("X-Forwarded-Proto");
				if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
					scheme = request.getScheme();
				}
				return scheme;
			}
		};
		super.service(requestProxyWrapper, response);
	}
}

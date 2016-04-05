package com.constellio.app.servlet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import org.apache.chemistry.opencmis.server.impl.browser.CmisBrowserBindingServlet;

public class ConstellioCmisBrowserBindingServlet extends CmisBrowserBindingServlet {

	@Override
	public void init(ServletConfig config)
			throws ServletException {
		if (ConstellioCmisServices.enabled) {
			super.init(config);
		}
	}
}

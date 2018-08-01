package com.constellio.app.servlet;

import org.apache.chemistry.opencmis.server.impl.browser.CmisBrowserBindingServlet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

public class ConstellioCmisBrowserBindingServlet extends CmisBrowserBindingServlet {

	@Override
	public void init(ServletConfig config)
			throws ServletException {
		if (ConstellioCmisServices.enabled) {
			super.init(config);
		}
	}
}

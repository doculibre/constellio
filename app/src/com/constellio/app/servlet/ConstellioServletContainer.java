package com.constellio.app.servlet;

import javax.servlet.ServletException;

import org.glassfish.jersey.servlet.ServletContainer;
import org.glassfish.jersey.servlet.WebConfig;

public class ConstellioServletContainer extends ServletContainer {

	@Override
	protected void init(WebConfig webConfig)
			throws ServletException {
		if (isEnabled()) {
			super.init(webConfig);
		}
	}

	private boolean isEnabled() {
		return "true".equals(System.getProperty("driverEnabled"));
	}
}

package com.constellio.app.servlet;

import javax.servlet.ServletContextEvent;

import org.apache.chemistry.opencmis.server.impl.CmisRepositoryContextListener;

public class ConstellioCmisRepositoryContextListener extends CmisRepositoryContextListener {

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		if (isEnabled()) {
			super.contextDestroyed(sce);
		}
	}

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		if (isEnabled()) {
			super.contextInitialized(sce);
		}
	}

	private boolean isEnabled() {
		return "true".equals(System.getProperty("cmisEnabled"));
	}
}

package com.constellio.app.servlet;

import javax.servlet.ServletContextAttributeEvent;
import javax.servlet.ServletContextAttributeListener;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.sun.xml.ws.transport.http.servlet.WSServletContextListener;

public class ConstellioWSServletContextListener implements ServletContextAttributeListener, ServletContextListener {

	WSServletContextListener nestedListener = new WSServletContextListener();

	@Override
	public void attributeAdded(ServletContextAttributeEvent event) {
		if (isEnabled()) {
			nestedListener.attributeAdded(event);
		}
	}

	@Override
	public void attributeRemoved(ServletContextAttributeEvent event) {
		if (isEnabled()) {
			nestedListener.attributeRemoved(event);
		}
	}

	@Override
	public void attributeReplaced(ServletContextAttributeEvent event) {
		if (isEnabled()) {
			nestedListener.attributeReplaced(event);
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent event) {
		if (isEnabled()) {
			nestedListener.contextDestroyed(event);
		}
	}

	@Override
	public void contextInitialized(ServletContextEvent event) {
		if (isEnabled()) {
			nestedListener.contextInitialized(event);
		}
	}

	private boolean isEnabled() {
		return "true".equals(System.getProperty("driverEnabled"));
	}
}

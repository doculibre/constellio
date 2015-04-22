/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
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

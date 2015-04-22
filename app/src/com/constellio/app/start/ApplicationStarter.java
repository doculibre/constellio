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
package com.constellio.app.start;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.resource.ResourceCollection;
import org.eclipse.jetty.webapp.Configuration;
import org.eclipse.jetty.webapp.FragmentConfiguration;
import org.eclipse.jetty.webapp.MetaInfConfiguration;
import org.eclipse.jetty.webapp.TagLibConfiguration;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.webapp.WebInfConfiguration;
import org.eclipse.jetty.webapp.WebXmlConfiguration;

public class ApplicationStarter {

	private static Server server;

	private ApplicationStarter() {

	}

	public static void startApplication(boolean joinServerThread, File webContentDir, int port) {

		List<String> resources = new ArrayList<String>();
		resources.add(webContentDir.getAbsolutePath());

		server = new Server(port);

		// Static file handler
		WebAppContext handler = new WebAppContext();
		handler.setConfigurations(new Configuration[] { new WebXmlConfiguration(), new WebInfConfiguration(),
				new TagLibConfiguration(), new MetaInfConfiguration(), new FragmentConfiguration() });
		handler.setContextPath("/constellio");

		handler.setBaseResource(new ResourceCollection(resources.toArray(new String[0])));

		handler.setParentLoaderPriority(true);
		handler.setClassLoader(Thread.currentThread().getContextClassLoader());

		server.setHandler(handler);
		try {
			server.start();
		} catch (Exception e) {
			throw new ApplicationStarterRuntimeException(e);
		}

		if (joinServerThread) {
			try {
				server.join();
			} catch (InterruptedException e) {
				throw new ApplicationStarterRuntimeException(e);
			}
		}
	}

	public static void stopApplication() {
		try {
			server.stop();
		} catch (Exception e) {
			throw new ApplicationStarterRuntimeException(e);
		}
	}

}

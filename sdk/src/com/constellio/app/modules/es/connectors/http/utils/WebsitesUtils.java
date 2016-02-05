package com.constellio.app.modules.es.connectors.http.utils;

import java.io.File;
import java.util.EnumSet;

import javax.servlet.DispatcherType;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import com.constellio.sdk.tests.AbstractConstellioTest;

public class WebsitesUtils {

	public static Server startWebsiteInState1() {
		return startWebsite("animalsStateV1");
	}

	public static Server startWebsiteInState2() {
		return startWebsite("animalsStateV2");
	}

	public static Server startWebsiteInState3WithDuplicates() {
		return startWebsite("animalsStateV3_WithDuplicates");
	}

	public static Server startWebsiteInState4WithDuplicatesModified() {
		return startWebsite("animalsStateV4_WithDuplicatesModified");
	}

	public static Server startWebsiteInState1Ntlm() {
		String name = "animalsStateV1";
		
		ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
		context.setContextPath("/");
		context.addFilter(NtlmAuthenticationFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));

		DefaultServlet defaultServlet = new DefaultServlet();
		ServletHolder servletHolder = new ServletHolder("default", defaultServlet);
		String ressourcePath = getFile(name).getAbsolutePath();
		servletHolder.setInitParameter("resourceBase", ressourcePath);
		context.addServlet(servletHolder, "/*");
		
		Server server = startWebsite(name, context);

		return server;
	}
	
	public static Server startWebsiteInState5() {
		return startWebsite("animalsStateV5");
	}

	public static Server startWebsite(String name, Handler... handlers) {
		File file = getFile(name);
		return startWebsite(file, handlers);
	}

	public static Server startWebsite(File file, Handler... handlers) {
		Server server = new Server();
		SelectChannelConnector connector = new SelectChannelConnector();
		connector.setPort(4242);
		server.addConnector(connector);

		ResourceHandler resourceHandler = new ResourceHandler();
		resourceHandler.setDirectoriesListed(true);
		resourceHandler.setWelcomeFiles(new String[] { "index.html" });
		resourceHandler.setResourceBase(file.getAbsolutePath());

		HandlerList serverHandlers = new HandlerList();
		for (Handler paramHandler : handlers) {
			serverHandlers.addHandler(paramHandler);
		}
		serverHandlers.addHandler(resourceHandler);

		server.setHandler(serverHandlers);

		try {
			server.start();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return server;
	}

	private static File getFile(String name) {
		File resourcesDir = AbstractConstellioTest.getResourcesDir();
		String pathInResourcesDir = WebsitesUtils.class.getName().replace(".", File.separator) + File.separator + name;
		return new File(resourcesDir, pathInResourcesDir);
	}

}

package com.constellio.app.start;

import java.io.File;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.Servlet;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.bio.SocketConnector;
import org.eclipse.jetty.server.ssl.SslSocketConnector;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.ResourceCollection;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.webapp.Configuration;
import org.eclipse.jetty.webapp.FragmentConfiguration;
import org.eclipse.jetty.webapp.MetaInfConfiguration;
import org.eclipse.jetty.webapp.TagLibConfiguration;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.webapp.WebInfConfiguration;
import org.eclipse.jetty.webapp.WebXmlConfiguration;

import com.constellio.model.conf.FoldersLocator;

public class ApplicationStarter {

	private static int REQUEST_HEADER_SIZE = 32 * 1024;

	private static Server server;
	private static WebAppContext handler;
	private static Map<String, List<Servlet>> servletMappings = new HashMap<>();
	private static Map<String, List<Filter>> filterMappings = new HashMap<>();

	private ApplicationStarter() {
	}

	public static void startApplication(boolean joinServerThread, File webContentDir, int port) {
		startApplication(new ApplicationStarterParams().setJoinServerThread(joinServerThread).setWebContentDir(webContentDir)
				.setPort(port));
	}

	public static void startApplication(boolean joinServerThread, File webContentDir, int port, String sslPassword) {
		startApplication(new ApplicationStarterParams().setJoinServerThread(joinServerThread).setWebContentDir(webContentDir)
				.setPort(port).setSSLWithKeystorePassword(sslPassword));
	}

	public static void startApplication(ApplicationStarterParams params) {

		List<String> resources = new ArrayList<String>();
		resources.add(params.getWebContentDir().getAbsolutePath());

		server = newServer(params);

		// Static file handler
		handler = new WebAppContext();
		handler.setConfigurations(new Configuration[] { new WebXmlConfiguration(), new WebInfConfiguration(),
				new TagLibConfiguration(), new MetaInfConfiguration(), new FragmentConfiguration() });
		handler.setContextPath("/constellio");

		handler.setBaseResource(new ResourceCollection(resources.toArray(new String[0])));

		handler.setParentLoaderPriority(true);
		handler.setClassLoader(Thread.currentThread().getContextClassLoader());

		server.setHandler(handler);
		try {
			server.start();

			for (String pathSpec : filterMappings.keySet()) {
				List<Filter> filters = filterMappings.get(pathSpec);
				for (Filter filter : filters) {
					handler.addFilter(new FilterHolder(filter), pathSpec, EnumSet.allOf(DispatcherType.class));
				}
			}

			for (String pathSpec : servletMappings.keySet()) {
				List<Servlet> servlets = servletMappings.get(pathSpec);
				for (Servlet servlet : servlets) {
					handler.addServlet(new ServletHolder(servlet), pathSpec);
				}
			}
		} catch (Exception e) {
			throw new ApplicationStarterRuntimeException(e);
		}

		if (params.isJoinServerThread()) {
			try {
				server.join();
			} catch (InterruptedException e) {
				throw new ApplicationStarterRuntimeException(e);
			}
		}
	}

	private static Server newServer(ApplicationStarterParams params) {
		if (params.isSSL()) {
			return getSslServer(params);
		} else {
			SocketConnector connector = new SocketConnector();
			connector.setPort(params.getPort());
			connector.setRequestHeaderSize(REQUEST_HEADER_SIZE);

			Server server =  new Server();
			server.setConnectors(new Connector[] { connector });
			return server;
		}
	}

	public static void stopApplication() {
		filterMappings.clear();
		servletMappings.clear();
		handler = null;
		try {
			server.stop();
		} catch (Exception e) {
			throw new ApplicationStarterRuntimeException(e);
		}
	}

	private static Server getSslServer(ApplicationStarterParams params) {
		Server sslServer = new Server();

		String keystorePath = new FoldersLocator().getKeystoreFile().getAbsolutePath();
		SslContextFactory sslContextFactory = new SslContextFactory(keystorePath);
		sslContextFactory.setKeyStorePassword(params.getKeystorePassword());
		sslContextFactory.addExcludeProtocols("SSLv3", "SSLv2", "SSLv2Hello");

		sslContextFactory.setExcludeCipherSuites(
				"SSL_RSA_WITH_DES_CBC_SHA",
				"SSL_DHE_RSA_WITH_DES_CBC_SHA",
				"SSL_DHE_DSS_WITH_DES_CBC_SHA",
				"SSL_RSA_EXPORT_WITH_RC4_40_MD5",
				"SSL_RSA_EXPORT_WITH_DES40_CBC_SHA",
				"SSL_DHE_RSA_EXPORT_WITH_DES40_CBC_SHA",
				"SSL_DHE_RSA_WITH_3DES_EDE_CBC_SHA",
				"SSL_DHE_DSS_WITH_3DES_EDE_CBC_SHA",
				"TLS_DHE_RSA_WITH_AES_256_CBC_SHA256",
				"TLS_DHE_DSS_WITH_AES_256_CBC_SHA256",
				"TLS_DHE_RSA_WITH_AES_256_CBC_SHA",
				"TLS_DHE_DSS_WITH_AES_256_CBC_SHA",
				"TLS_DHE_RSA_WITH_AES_128_CBC_SHA256",
				"TLS_DHE_DSS_WITH_AES_128_CBC_SHA256",
				"TLS_DHE_RSA_WITH_AES_128_CBC_SHA",
				"TLS_DHE_DSS_WITH_AES_128_CBC_SHA"
		);

		SslSocketConnector connector = new SslSocketConnector(sslContextFactory);
		connector.setPort(params.getPort());

		connector.setRequestHeaderSize(REQUEST_HEADER_SIZE);

		sslServer.setConnectors(new Connector[] { connector });

		return sslServer;
	}

	public static void registerServlet(String pathRelativeToConstellioContext, Servlet servlet) {
		if (handler == null) {
			if (!servletMappings.containsKey(pathRelativeToConstellioContext)) {
				servletMappings.put(pathRelativeToConstellioContext, new ArrayList<Servlet>());
			}
			servletMappings.get(pathRelativeToConstellioContext).add(servlet);
		} else {
			handler.addServlet(new ServletHolder(servlet), pathRelativeToConstellioContext);
		}
	}

	public static void registerFilter(String pathRelativeToConstellioContext, Filter filter) {
		if (handler == null) {
			if (!filterMappings.containsKey(pathRelativeToConstellioContext)) {
				filterMappings.put(pathRelativeToConstellioContext, new ArrayList<Filter>());
			}
			filterMappings.get(pathRelativeToConstellioContext).add(filter);
		} else {
			handler.addFilter(new FilterHolder(filter), pathRelativeToConstellioContext, EnumSet.allOf(DispatcherType.class));
		}
	}

	public static void resetServlets() {
		servletMappings.clear();
	}

	public static void resetFilters() {
		filterMappings.clear();
	}
}
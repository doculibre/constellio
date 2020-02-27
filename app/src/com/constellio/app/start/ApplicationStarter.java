package com.constellio.app.start;

import com.constellio.model.conf.FoldersLocator;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.FilterMapping;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlet.ServletMapping;
import org.eclipse.jetty.util.resource.ResourceCollection;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.webapp.Configuration;
import org.eclipse.jetty.webapp.FragmentConfiguration;
import org.eclipse.jetty.webapp.MetaInfConfiguration;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.webapp.WebInfConfiguration;
import org.eclipse.jetty.webapp.WebXmlConfiguration;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.Servlet;
import java.io.File;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApplicationStarter {

	private static int REQUEST_HEADER_SIZE = 128 * 1024;

	private static Server server;
	private static WebAppContext handler;
	private static Map<String, ServletHolder> servletMappings = new HashMap<>();
	private static Map<String, List<FilterHolder>> filterMappings = new HashMap<>();

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
		handler.setConfigurations(new Configuration[]{new WebXmlConfiguration(), new WebInfConfiguration(), new MetaInfConfiguration(), new FragmentConfiguration()});
		handler.setContextPath("/constellio");

		handler.setBaseResource(new ResourceCollection(resources.toArray(new String[0])));

		handler.setParentLoaderPriority(true);
		handler.setClassLoader(Thread.currentThread().getContextClassLoader());

		server.setHandler(handler);

		try {
			server.start();

			for (String pathSpec : filterMappings.keySet()) {
				List<FilterHolder> filters = filterMappings.get(pathSpec);
				for (FilterHolder filter : filters) {
					handler.addFilter(filter, pathSpec, EnumSet.allOf(DispatcherType.class));
				}
			}

			for (String pathSpec : servletMappings.keySet()) {
				ServletHolder servlet = servletMappings.get(pathSpec);
				handler.addServlet(servlet, pathSpec);
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
			QueuedThreadPool threadPool = new QueuedThreadPool(5000, 10);
			Server server = new Server(threadPool);

			HttpConfiguration http_config = new HttpConfiguration();
			http_config.setOutputBufferSize(32768);
			http_config.setRequestHeaderSize(REQUEST_HEADER_SIZE);

			ServerConnector http = new ServerConnector(server, new HttpConnectionFactory(http_config));
			http.setPort(params.getPort());
			http.setIdleTimeout(30000);

			server.setConnectors(new Connector[]{http});
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
		sslServer.setSendServerVersion(false);

		String keystorePath = new FoldersLocator().getKeystoreFile().getAbsolutePath();
		SslContextFactory sslContextFactory = new SslContextFactory(keystorePath);
		sslContextFactory.setKeyStorePassword(params.getKeystorePassword());
		sslContextFactory.addExcludeProtocols("SSLv3", "SSLv2", "SSLv2Hello", "TLSv1", "TLSv1.1");

		sslContextFactory.setIncludeCipherSuites(
				"TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256",
				"TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384",
				"TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256",
				"TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384",
				"TLS_DHE_RSA_WITH_AES_128_GCM_SHA256",
				"TLS_DHE_DSS_WITH_AES_128_GCM_SHA256",
				"TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA",
				"TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256",
				"TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA",
				"TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384",
				"TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256",
				"TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA",
				"TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384",
				"TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA",
				"TLS_DHE_RSA_WITH_AES_128_GCM_SHA256",
				"TLS_DHE_RSA_WITH_AES_128_CBC_SHA",
				"TLS_DHE_RSA_WITH_AES_128_CBC_SHA256",
				"TLS_DHE_RSA_WITH_AES_256_GCM_SHA384",
				"TLS_DHE_RSA_WITH_AES_256_CBC_SHA",
				"TLS_DHE_RSA_WITH_AES_256_CBC_SHA256"
		);

		HttpConfiguration https_config = new HttpConfiguration();
		https_config.setOutputBufferSize(32768);
		https_config.setRequestHeaderSize(REQUEST_HEADER_SIZE);

		SecureRequestCustomizer src = new SecureRequestCustomizer();
		src.setStsMaxAge(2000);
		src.setStsIncludeSubDomains(true);
		https_config.addCustomizer(src);

		ServerConnector https = new ServerConnector(sslServer,
				new SslConnectionFactory(sslContextFactory, HttpVersion.HTTP_1_1.asString()),
				new HttpConnectionFactory(https_config));
		https.setPort(params.getPort());
		https.setIdleTimeout(30000);


		sslServer.setConnectors(new Connector[]{https});

		return sslServer;
	}

	public static void registerServlet(String pathRelativeToConstellioContext, ServletHolder servletHolder) {
		if (!servletMappings.containsKey(pathRelativeToConstellioContext)) {
			servletMappings.put(pathRelativeToConstellioContext, servletHolder);
			if (handler != null) {
				handler.addServlet(servletHolder, pathRelativeToConstellioContext);
			}
		}
	}

	public static void registerServlet(String pathRelativeToConstellioContext, Servlet servlet) {
		registerServlet(pathRelativeToConstellioContext, new ServletHolder(servlet));
	}

	public static void registerFilter(String pathRelativeToConstellioContext, Filter filter) {
		registerFilter(pathRelativeToConstellioContext, new FilterHolder(filter));
	}

	public static void registerFilter(String pathRelativeToConstellioContext, FilterHolder filterHolder) {
		if (handler == null) {
			if (!filterMappings.containsKey(pathRelativeToConstellioContext)) {
				filterMappings.put(pathRelativeToConstellioContext, new ArrayList<FilterHolder>());
			}
			filterMappings.get(pathRelativeToConstellioContext).add(filterHolder);
		} else {
			handler.addFilter(filterHolder, pathRelativeToConstellioContext, EnumSet.allOf(DispatcherType.class));
		}
	}

	public static void resetServlets() {
		if (handler != null) {
			handler.getServletHandler().setServlets(new ServletHolder[0]);
			handler.getServletHandler().setServletMappings(new ServletMapping[0]);
		}
		servletMappings.clear();
	}

	public static void resetFilters() {
		if (handler != null) {
			handler.getServletHandler().setFilters(new FilterHolder[0]);
			handler.getServletHandler().setFilterMappings(new FilterMapping[0]);
		}
		filterMappings.clear();
	}
}

package com.constellio.app.start;

import com.constellio.data.conf.FoldersLocator;
import com.constellio.data.utils.dev.Toggle;
import com.google.common.collect.Lists;
import org.eclipse.jetty.http.HttpCookie;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.ForwardedRequestCustomizer;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.ErrorHandler;
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
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
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

		handler.setErrorHandler(new ErrorHandler() {
			@Override
			protected void writeErrorPage(HttpServletRequest request, Writer writer, int code, String message,
										  boolean showStacks) throws IOException {
				if (Toggle.SHOW_STACK_TRACE_UPON_ERRORS.isEnabled()) {
					super.writeErrorPage(request, writer, code, message, showStacks);
				}
			}
		});

		handler.setBaseResource(new ResourceCollection(resources.toArray(new String[0])));

		handler.setParentLoaderPriority(true);
		handler.setClassLoader(Thread.currentThread().getContextClassLoader());

		handler.getSessionHandler().getSessionCookieConfig().setHttpOnly(true);

		if (params.isSSL() || params.isHttpsViaProxy()) {
			handler.getSessionHandler().getSessionCookieConfig().setSecure(true);
			handler.getSessionHandler().getSessionCookieConfig().setComment(HttpCookie.SAME_SITE_NONE_COMMENT);
		}
		//https://bugs.eclipse.org/bugs/show_bug.cgi?id=326612 secure cookie is already enable automatically

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
			server.setAttribute("org.eclipse.jetty.server.Request.maxFormContentSize", 1000000000);

			HttpConfiguration http_config = new HttpConfiguration();
			http_config.setOutputBufferSize(32768);
			http_config.setSendServerVersion(false);
			http_config.setRequestHeaderSize(REQUEST_HEADER_SIZE);
			http_config.addCustomizer(new ForwardedRequestCustomizer());

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

		String keystorePath = new FoldersLocator().getKeystoreFile().getAbsolutePath();
		SslContextFactory sslContextFactory = new SslContextFactory.Server();
		sslContextFactory.setKeyStorePath(keystorePath);
		sslContextFactory.setKeyStorePassword(params.getKeystorePassword());
		sslContextFactory.addExcludeProtocols("SSLv3", "SSLv2", "SSLv2Hello", "TLSv1", "TLSv1.1");
		sslContextFactory.setSessionCachingEnabled(true);

		sslContextFactory.setIncludeCipherSuites(
				"TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256",
				"TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384",
				"TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256",
				"TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384",
				//"TLS_DHE_DSS_WITH_AES_128_GCM_SHA256",
				"TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA",
				"TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256",
				"TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA",
				"TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384",
				"TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256",
				"TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA",
				"TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384",
				"TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA",
				//"TLS_DHE_RSA_WITH_AES_128_CBC_SHA",
				//"TLS_DHE_RSA_WITH_AES_256_CBC_SHA",
				//"TLS_DHE_RSA_WITH_AES_256_GCM_SHA384",
				//"TLS_DHE_RSA_WITH_AES_128_GCM_SHA256",
				"TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256",
				"TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384"
		);

		HttpConfiguration https_config = new HttpConfiguration();
		https_config.setOutputBufferSize(32768);
		https_config.setSendServerVersion(false);
		https_config.setRequestHeaderSize(REQUEST_HEADER_SIZE);

		SecureRequestCustomizer src = new SecureRequestCustomizer();
		src.setStsMaxAge(31536000);
		src.setStsIncludeSubDomains(true);
		https_config.addCustomizer(src);

		https_config.addCustomizer(new ForwardedRequestCustomizer());

		ServerConnector https = new ServerConnector(sslServer,
				new SslConnectionFactory(sslContextFactory, HttpVersion.HTTP_1_1.asString()),
				new HttpConnectionFactory(https_config));
		https.setPort(params.getPort());
		https.setIdleTimeout(30000);


		sslServer.setConnectors(new Connector[]{https});

		return sslServer;
	}

	public static synchronized void registerServlet(String pathRelativeToConstellioContext,
													ServletHolder servletHolder) {
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


	public static void registerFilterIfEmpty(String pathRelativeToConstellioContext, Filter filter) {
		registerFilterIfEmpty(pathRelativeToConstellioContext, new FilterHolder(filter));
	}

	public static synchronized void registerFilter(String pathRelativeToConstellioContext, FilterHolder filterHolder) {
		if (handler == null) {
			if (!filterMappings.containsKey(pathRelativeToConstellioContext)) {
				filterMappings.put(pathRelativeToConstellioContext, new ArrayList<FilterHolder>());
			}
			filterMappings.get(pathRelativeToConstellioContext).add(filterHolder);
		} else {
			handler.addFilter(filterHolder, pathRelativeToConstellioContext, EnumSet.allOf(DispatcherType.class));
		}
	}

	public static synchronized void registerFilterIfEmpty(String pathRelativeToConstellioContext,
														  FilterHolder filterHolder) {
		if (handler == null) {
			if (!filterMappings.containsKey(pathRelativeToConstellioContext)) {
				filterMappings.put(pathRelativeToConstellioContext, new ArrayList<FilterHolder>());
				filterMappings.get(pathRelativeToConstellioContext).add(filterHolder);
			}
			filterMappings.get(pathRelativeToConstellioContext).add(filterHolder);
		} else {
			handler.addFilter(filterHolder, pathRelativeToConstellioContext, EnumSet.allOf(DispatcherType.class));
		}
	}

	public static void replaceServlet(String pathRelativeToConstellioContext, Servlet servlet) {
		replaceServlet(pathRelativeToConstellioContext, new ServletHolder(servlet));
	}

	public static void replaceServlet(String pathRelativeToConstellioContext, ServletHolder servletHolder) {
		ServletHolder oldServletHolder = servletMappings.get(pathRelativeToConstellioContext);
		servletMappings.put(pathRelativeToConstellioContext, servletHolder);
		if (handler != null) {
			for (int i = 0; i < handler.getServletHandler().getServlets().length; i++) {
				if (handler.getServletHandler().getServlets()[i].getName().equals(oldServletHolder.getName())) {
					handler.getServletHandler().getServlets()[i] = servletHolder;
				}
			}
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

	public static List<ServletHolder> getServletHolders() {
		if (handler != null) {
			return Lists.newArrayList(handler.getServletHandler().getServlets());
		}
		return new ArrayList<>(servletMappings.values());
	}
}

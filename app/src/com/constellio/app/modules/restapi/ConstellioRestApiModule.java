package com.constellio.app.modules.restapi;

import com.constellio.app.entities.modules.InstallableSystemModuleExcludedFromSSO;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.entities.navigation.NavigationConfig;
import com.constellio.app.modules.restapi.core.config.RestApiResourceConfig;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.configs.SystemConfiguration;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.glassfish.jersey.servlet.ServletContainer;

import java.util.Collections;
import java.util.List;

@Slf4j
public class ConstellioRestApiModule extends InstallableSystemModuleExcludedFromSSO {

	public static final String ID = "restapi";
	public static final String NAME = "Constellio Rest Api";

	private static final String SERVICE_PATH = "/rest/v1/";

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public String getPublisher() {
		return "Constellio";
	}

	@Override
	public boolean isComplementary() {
		return false;
	}

	@Override
	public List<String> getDependencies() {
		return Collections.emptyList();
	}

	@Override
	public List<SystemConfiguration> getConfigurations() {
		return RestApiConfigs.configurations;
	}

	@Override
	public List<MigrationScript> getMigrationScripts() {
		return Collections.emptyList();
	}

	@Override
	public void configureNavigation(NavigationConfig config) {
	}

	@Override
	public void start(String collection, AppLayerFactory appLayerFactory) {
	}

	@Override
	public void stop(String collection, AppLayerFactory appLayerFactory) {
	}

	@Override
	public String getServicePath() {
		return SERVICE_PATH;
	}

	@Override
	public ServletHolder configureServlet(AppLayerFactory appLayerFactory) {
		ServletHolder servletHolder = new ServletHolder(new ServletContainer(new RestApiResourceConfig()));
		servletHolder.setInitOrder(0);
		servletHolder.setInitParameter("jersey.config.server.provider.packages", "com.constellio.app.modules.restapi");

		return servletHolder;
	}

	@Override
	public List<FilterHolder> configureFilters(AppLayerFactory appLayerFactory) {
		FilterHolder filterHolder = new FilterHolder(new CrossOriginFilter());
		filterHolder.setInitParameter(CrossOriginFilter.ALLOWED_METHODS_PARAM, "GET,HEAD,POST,PUT,PATCH,DELETE,OPTIONS");
		filterHolder.setInitParameter(CrossOriginFilter.ALLOWED_HEADERS_PARAM,
				"X-Requested-With,Content-Type,Accept,Origin,Constellio-Flushing-Mode,Host,If-Match,ETag");
		filterHolder.setInitParameter(CrossOriginFilter.EXPOSED_HEADERS_PARAM, "ETag");
		filterHolder.setInitParameter(CrossOriginFilter.CHAIN_PREFLIGHT_PARAM, "false");

		String allowedOrigins = new RestApiConfigs(appLayerFactory).getCorsAllowedOrigins();
		if (!Strings.isNullOrEmpty(allowedOrigins) && !allowedOrigins.equals("*")) {
			filterHolder.setInitParameter(CrossOriginFilter.ALLOWED_ORIGINS_PARAM, allowedOrigins);
		}

		return Collections.singletonList(filterHolder);
	}

	@Override
	public void addDemoData(String collection, AppLayerFactory appLayerFactory) {
	}

	@Override
	public void start(AppLayerFactory appLayerFactory) {
		super.start(appLayerFactory);
		log.info("Rest Api Module started");
	}

	@Override
	public void stop(AppLayerFactory appLayerFactory) {
	}
}

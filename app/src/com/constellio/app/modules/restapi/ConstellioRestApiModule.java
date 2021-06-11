package com.constellio.app.modules.restapi;

import com.constellio.app.entities.modules.InstallableSystemModule;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.entities.modules.ModuleExcludedFromSSO;
import com.constellio.app.entities.navigation.NavigationConfig;
import com.constellio.app.extensions.AppLayerCollectionExtensions;
import com.constellio.app.modules.restapi.core.config.RestApiResourceConfigV1;
import com.constellio.app.modules.restapi.core.config.RestApiResourceConfigV2;
import com.constellio.app.modules.restapi.extensions.RestApiModuleExtensions;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.start.ApplicationStarter;
import com.constellio.model.entities.configs.SystemConfiguration;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;

@Slf4j
public class ConstellioRestApiModule implements InstallableSystemModule, ModuleExcludedFromSSO {

	public static final String ID = "restapi";
	public static final String NAME = "Constellio Rest Api";

	private static final String SERVICE_PATH_V1 = "/rest/v1/";
	private static final String SERVICE_PATH_V2 = "/rest/v2/";

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
	public Map<String, List<String>> getPermissions() {
		return null;
	}

	@Override
	public List<String> getRolesForCreator() {
		return null;
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
		setupAppLayerExtensions(collection, appLayerFactory);
	}

	@Override
	public void stop(String collection, AppLayerFactory appLayerFactory) {
	}

	@Override
	public List<String> getServicePaths() {
		return asList(SERVICE_PATH_V1, SERVICE_PATH_V2);
	}

	@Override
	public void addDemoData(String collection, AppLayerFactory appLayerFactory) {
	}

	@Override
	public void start(AppLayerFactory appLayerFactory) {
		String fullServicePathV1 = SERVICE_PATH_V1 + "*";
		ApplicationStarter.registerServlet(fullServicePathV1, configureServlet(new RestApiResourceConfigV1()));
		ApplicationStarter.registerFilter(fullServicePathV1, configureFilters(appLayerFactory));

		String fullServicePathV2 = SERVICE_PATH_V2 + "*";
		ApplicationStarter.registerServlet(fullServicePathV2, configureServlet(new RestApiResourceConfigV2()));
		ApplicationStarter.registerFilter(fullServicePathV2, configureFilters(appLayerFactory));

		log.info("Rest Api Module started");
	}

	@Override
	public void stop(AppLayerFactory appLayerFactory) {
	}

	private ServletHolder configureServlet(ResourceConfig resourceConfig) {
		return new ServletHolder(new ServletContainer(resourceConfig));
	}

	private FilterHolder configureFilters(AppLayerFactory appLayerFactory) {
		FilterHolder filterHolder = new FilterHolder(new CrossOriginFilter());
		filterHolder.setInitParameter(CrossOriginFilter.ALLOWED_METHODS_PARAM, "GET,HEAD,POST,PUT,PATCH,DELETE,OPTIONS");
		filterHolder.setInitParameter(CrossOriginFilter.ALLOWED_HEADERS_PARAM,
				"X-Requested-With,Content-Type,Accept,Origin,Constellio-Flushing-Mode,Host,If-Match,ETag,Authorization");
		filterHolder.setInitParameter(CrossOriginFilter.EXPOSED_HEADERS_PARAM, "ETag");
		filterHolder.setInitParameter(CrossOriginFilter.CHAIN_PREFLIGHT_PARAM, "false");

		String allowedOrigins = new RestApiConfigs(appLayerFactory).getCorsAllowedOrigins();
		if (!Strings.isNullOrEmpty(allowedOrigins) && !allowedOrigins.equals("*")) {
			filterHolder.setInitParameter(CrossOriginFilter.ALLOWED_ORIGINS_PARAM, allowedOrigins);
		}
		return filterHolder;
	}

	private void setupAppLayerExtensions(String collection, AppLayerFactory appLayerFactory) {
		AppLayerCollectionExtensions extensions = appLayerFactory.getExtensions().forCollection(collection);
		extensions.registerModuleExtensionsPoint(ID, new RestApiModuleExtensions());
	}
}

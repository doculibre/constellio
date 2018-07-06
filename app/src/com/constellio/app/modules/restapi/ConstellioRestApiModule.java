package com.constellio.app.modules.restapi;

import com.constellio.app.entities.modules.InstallableSystemModule;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.entities.navigation.NavigationConfig;
import com.constellio.app.modules.restapi.core.config.RestApiResourceConfig;
import com.constellio.app.modules.rm.ConstellioRMModule;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.start.ApplicationStarter;
import com.constellio.model.entities.configs.SystemConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.glassfish.jersey.servlet.ServletContainer;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
public class ConstellioRestApiModule implements InstallableSystemModule {

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
        return Collections.emptyList();
    }

    @Override
    public Map<String, List<String>> getPermissions() {
        return Collections.emptyMap();
    }

    @Override
    public List<String> getRolesForCreator() {
        return Collections.emptyList();
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
    public void addDemoData(String collection, AppLayerFactory appLayerFactory) {
    }

    @Override
    public void start(AppLayerFactory appLayerFactory) {
        log.info("Rest Api Module started");

        ServletHolder servletHolder = new ServletHolder(new ServletContainer(new RestApiResourceConfig()));
        servletHolder.setInitOrder(0);
        servletHolder.setInitParameter("jersey.config.server.provider.packages", "com.constellio.app.modules.restapi");

        ApplicationStarter.registerServlet(SERVICE_PATH + "*", servletHolder);
        ApplicationStarter.registerFilter(SERVICE_PATH + "*", new CrossOriginFilter());
    }

    @Override
    public void stop(AppLayerFactory appLayerFactory) {
    }
}

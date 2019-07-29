package com.constellio.app.entities.modules;

import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.start.ApplicationStarter;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletHolder;

import java.util.List;

public abstract class InstallableSystemModuleExcludedFromSSO implements InstallableSystemModule {

	@Override
	public void start(AppLayerFactory appLayerFactory) {
		String fullServicePath = getServicePath() + "*";
		ApplicationStarter.registerServlet(fullServicePath, configureServlet(appLayerFactory));
		for (FilterHolder filter : configureFilters(appLayerFactory)) {
			ApplicationStarter.registerFilter(fullServicePath, filter);
		}
	}

	@Override
	public void stop(AppLayerFactory appLayerFactory) {
	}

	@Override
	public void start(String collection, AppLayerFactory appLayerFactory) {
	}

	@Override
	public void stop(String collection, AppLayerFactory appLayerFactory) {
	}

	public abstract String getServicePath();

	public abstract ServletHolder configureServlet(AppLayerFactory appLayerFactory);

	public abstract List<FilterHolder> configureFilters(AppLayerFactory appLayerFactory);

}

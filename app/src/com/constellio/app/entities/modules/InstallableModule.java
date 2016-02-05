package com.constellio.app.entities.modules;

import java.util.List;

import com.constellio.app.entities.navigation.NavigationConfig;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.modules.Module;

public interface InstallableModule extends Module {
	List<MigrationScript> getMigrationScripts();

	void configureNavigation(NavigationConfig config);

	void start(String collection, AppLayerFactory appLayerFactory);

	void stop(String collection, AppLayerFactory appLayerFactory);

	void addDemoData(String collection, AppLayerFactory appLayerFactory);
}
